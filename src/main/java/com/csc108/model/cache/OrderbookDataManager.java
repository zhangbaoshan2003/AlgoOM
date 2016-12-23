package com.csc108.model.cache;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.EventType;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnection;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnectionFactory;
import com.csc108.infrastructure.pooledActiveMQ.PooledSession;
import com.csc108.log.LogFactory;
import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.market.*;
import com.csc108.utility.Alert;
import com.csc108.utility.FormattedTable;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;

import javax.jms.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangbaoshan on 2016/5/31.
 */
public class OrderbookDataManager {
    private final String Level2DataTopicName = "quotahq";
    private final String MQ_RETROACTIVE = "?consumer.retroactive=true";
    private final Logger logger = LogFactory.getMarketDataActiveMqLogger();

    private PooledConnectionFactory connectionFactory ;
    private PooledSession session;
    private PooledConnection pooledConnection;
    private MessageProducer producer;
    private Destination dest;

    private final HashMap<String,String> hs_name_to_symbol_dic = new HashMap<>();

    private final ConcurrentHashMap<String,Security> securitiesSubscribed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,ArrayList<OrderHandler>> orderBookHandlerMap = new ConcurrentHashMap<>();

    private static OrderbookDataManager instance = new OrderbookDataManager();
    public static OrderbookDataManager getInstance(){
        return instance;
    }

    private OrderbookDataManager(){

    }

    private int MAX_ORDER_BOOK_SIZE=5;
    //time series order book indexed by seicurity ID,
    private final ConcurrentHashMap<String,CircularFifoQueue<OrderBook>> orderBooks = new ConcurrentHashMap<>();

    public OrderBook getLatestOrderBook(String securityID){
        if(orderBooks.keySet().contains(securityID)==false)
            return null;
        return orderBooks.get(securityID).get(orderBooks.get(securityID).size()-1);
    }

    public  ArrayList<OrderBook> getLatestOrderBooks() {
        ArrayList<OrderBook> orderBooksLatest = new ArrayList<>();
        orderBooks.values().forEach(x -> {
            OrderBook ob = x.get(x.size() - 1);
            orderBooksLatest.add(ob);
        });

        return orderBooksLatest;
    }

    public void addNewOrderBook(OrderBook orderBook){
        CircularFifoQueue<OrderBook> timeSeriesOrderBook =orderBooks.get(orderBook.getSecurity().getSymbol());
        if(timeSeriesOrderBook==null){
            timeSeriesOrderBook = new CircularFifoQueue<OrderBook>(MAX_ORDER_BOOK_SIZE);
            orderBooks.put(orderBook.getSecurity().getSymbol(),timeSeriesOrderBook);
        }
        timeSeriesOrderBook.add(orderBook);
    }

    private String generateFilter()
    {
        StringBuilder sb = new StringBuilder("myFilter in (");

        for (String key : securitiesSubscribed.keySet())
        {
            sb.append("'").append(String.format("hq%s%s", StringUtils.substringBefore(key, "."), StringUtils.substringAfter(key, "."))).append("',");
        }
        sb.append("#");
        //sb.replace(sb.length() - 1, 1, "0");

        return sb.toString().replace(",#",")");
    }

    private void registerOrderBookHandlerMap(String symbol,OrderHandler handler){
        if(orderBookHandlerMap.get(symbol)==null){
            orderBookHandlerMap.put(symbol,new ArrayList<>());
        }

        if(handler!=null){
            long exsitedNum = orderBookHandlerMap.get(symbol).stream().filter(x->x.getClientOrder().getClientOrderId()==handler.getClientOrder().getClientOrderId()).count();
            if(exsitedNum==0 && handler!=null){
                orderBookHandlerMap.get(symbol).add(handler);
            }
        }
    }

    public void publishData(String symbol,double preClosePx,double lasPx){
        try{
            //Destination dest = session.createTopic(Level2DataTopicName + MQ_RETROACTIVE);
            //MessageProducer producer= session.createProducer(dest);
            String quota = String.format("<Quot><head type=\"hq\" recordnum=\"1\"/><body><record stkcode=\"%s\" stkname=\"???????\" marketid=\"sh\" iopvvalue=\"0.0000\" isstop=\"F\" preclose=\"%f\" lastprice=\"%f\" openprice=\"10.300\" closeprice=\"10.390\" highestprice=\"10.410\" lowestprice=\"10.300\" donevolume=\"18936822\" turnover=\"195856019\" bidprice1=\"10.380\" askprice1=\"10.390\" bidvolume1=\"161100\" askvolume1=\"62290\" bidprice2=\"10.370\" askprice2=\"10.400\" bidvolume2=\"286700\" askvolume2=\"453533\" bidprice3=\"10.360\" askprice3=\"10.410\" bidvolume3=\"339292\" askvolume3=\"182800\" bidprice4=\"10.350\" askprice4=\"10.420\" bidvolume4=\"182100\" askvolume4=\"207600\" bidprice5=\"10.340\" askprice5=\"10.430\" bidvolume5=\"172100\" askvolume5=\"307300\" bidprice6=\"10.330\" askprice6=\"10.440\" bidvolume6=\"287400\" askvolume6=\"159000\" bidprice7=\"10.320\" askprice7=\"10.450\" bidvolume7=\"188903\" askvolume7=\"422200\" bidprice8=\"10.310\" askprice8=\"10.460\" bidvolume8=\"490300\" askvolume8=\"68600\" bidprice9=\"10.300\" askprice9=\"10.470\" bidvolume9=\"268200\" askvolume9=\"48100\" bidprice10=\"10.290\" askprice10=\"10.480\" bidvolume10=\"165300\" askvolume10=\"240419\" settleprice=\"0.00\" openinterest=\"0.00\" time=\"130100000\" /></body></Quot>",
                    symbol, preClosePx, lasPx);
            TextMessage txtMsg= session.createTextMessage(quota);
            txtMsg.setStringProperty("myFilter",String.format("hq%ssh",symbol));
            producer.send(dest, txtMsg);

        }catch (Exception ex){
            LogFactory.error("publishData error!", ex);
        }
    }

    public void publish_SH_SecurityData(String symbol,double preClosePx,double lastPx,double openPx,double closePx,double ap,double bp){
        publishSecurityData(symbol,"sh",preClosePx,lastPx,openPx,closePx,ap,bp);
    }

    public void publish_SZ_SecurityData(String symbol,double preClosePx,double lastPx,double openPx,double closePx,double ap,double bp){
        publishSecurityData(symbol,"sz",preClosePx,lastPx,openPx,closePx,ap,bp);
    }

    private void publishSecurityData(String symbol,String exchangeDest,double preClosePx,double lastPx,double openPx,double closePx,double ap,double bp){
        try{
            //Destination dest = session.createTopic(Level2DataTopicName + MQ_RETROACTIVE);
            //MessageProducer producer= session.createProducer(dest);
            String quota = String.format("<Quot><head type=\"hq\" recordnum=\"1\"/>" +
                            "<body><record stkcode=\"%s\" stkname=\"???????\" marketid=\"%s\" iopvvalue=\"0.0000\" " +
                            "isstop=\"F\" preclose=\"%f\" lastprice=\"%f\" " +
                            "openprice=\"%f\" closeprice=\"%f\" " +
                            "highestprice=\"10.410\" lowestprice=\"10.300\" donevolume=\"18936822\" turnover=\"195856019\" " +
                            "bidprice1=\"%f\" askprice1=\"%f\" bidvolume1=\"161100\" askvolume1=\"62290\" " +
                            "bidprice2=\"%f\" askprice2=\"%f\" bidvolume2=\"286700\" askvolume2=\"453533\" " +
                            "bidprice3=\"%f\" askprice3=\"%f\" bidvolume3=\"339292\" askvolume3=\"182800\" " +
                            "bidprice4=\"%f\" askprice4=\"%f\" bidvolume4=\"182100\" askvolume4=\"207600\" " +
                            "bidprice5=\"%f\" askprice5=\"%f\" bidvolume5=\"172100\" askvolume5=\"307300\" " +
                            "bidprice6=\"%f\" askprice6=\"%f\" bidvolume6=\"287400\" askvolume6=\"159000\" " +
                            "bidprice7=\"%f\" askprice7=\"%f\" bidvolume7=\"188903\" askvolume7=\"422200\" " +
                            "bidprice8=\"%f\" askprice8=\"%f\" bidvolume8=\"490300\" askvolume8=\"68600\" " +
                            "bidprice9=\"%f\" askprice9=\"%f\" bidvolume9=\"268200\" askvolume9=\"48100\" " +
                            "bidprice10=\"%f\" askprice10=\"%f\" bidvolume10=\"165300\" askvolume10=\"240419\" " +
                            "settleprice=\"0.00\" openinterest=\"0.00\" time=\"130100000\" /></body></Quot>",
                    symbol, exchangeDest,
                    preClosePx, lastPx,openPx,closePx,
                    bp,ap,
                    bp-0.01,ap+0.01,
                    bp-0.02,ap+0.02,
                    bp-0.03,ap+0.03,
                    bp-0.04,ap+0.04,
                    bp-0.05,ap+0.05,
                    bp-0.06,ap+0.06,
                    bp-0.07,ap+0.07,
                    bp-0.08,ap+0.08,
                    bp-0.09,ap+0.09);
            TextMessage txtMsg= session.createTextMessage(quota);
            txtMsg.setStringProperty("myFilter",String.format("hq%ssh",symbol));
            producer.send(dest, txtMsg);

        }catch (Exception ex){
            LogFactory.error("publishData error!", ex);
        }
    }

    private void broadcastOrderBookUpdatedEvent(OrderBook obChanged) {
        try{
            OrderBook localOrderBook = obChanged.clone();
            String securityChanged = localOrderBook.getSecurity().getSymbol();

            if(orderBookHandlerMap.get(securityChanged)!=null){
                Object[] handlers= orderBookHandlerMap.get(securityChanged).toArray();
                if(handlers!=null && handlers.length>0){
                    for (Object oHandler : handlers){
                        OrderHandler handler = (OrderHandler)oHandler;
                        OrderBookEvaluationData orderBookEvaluationData = new OrderBookEvaluationData(localOrderBook);
                        try {
                            DisruptorController controller = handler.getController();
                            if(controller!=null){
                                controller.enqueueEvent(EventType.EVALUATION, handler, orderBookEvaluationData);
                            }
                        } catch (Exception ex) {
                            LogFactory.error("orderBookUpdated for controller in queue error", ex);
                        }
                    }
                }
            }
        }catch (Exception ex){
            LogFactory.error("orderBookUpdated error",ex);
        }
    }

    private OrderBook buildEmptyOrderBook(String symbol) {
        List<Quote> ask = new ArrayList<Quote>();
        ask.add(new Quote(Double.NaN, Double.NaN));
        ask.add(new Quote(Double.NaN, Double.NaN));
        ask.add(new Quote(Double.NaN, Double.NaN));

        List<Quote> bid = new ArrayList<Quote>();
        bid.add(new Quote(Double.NaN, Double.NaN));
        bid.add(new Quote(Double.NaN, Double.NaN));
        bid.add(new Quote(Double.NaN, Double.NaN));

        OrderBook book = OrderBook.of();
        book.setAsk(ask);
        book.setBid(bid);

        book.setPreClose(Double.NaN);
        book.setHighestPrice(Double.NaN);
        book.setLastPrice(Double.NaN);
        book.setOpenPrice(Double.NaN);
        book.setSecurity(new Security(symbol, SecurityType.Stock));

        return book;
    }

    private void subscribeDefaultSecurity(){
        try{
            Document doc = new Document();
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build("configuration/defaultSecurityList.xml");
            List<Element> elementLis = doc.getRootElement().getChild("DefaultSecurity").getChildren("Security");
            for (Element e:elementLis){
                String securityCode = e.getChildText("Symbol");
                String securityHSName = e.getChildText("Name");
                Security security= new Security(securityCode, SecurityType.Stock);
                security.set_HSName(securityHSName);
                hs_name_to_symbol_dic.put(securityHSName,securityCode);
                OrderBook ob = buildEmptyOrderBook(securityCode);
                subscribeOrderBook(ob.getSecurity().getSymbol(),false,null);
            }

        }catch (Exception ex){
            LogFactory.error("Initialize default security subscription failed", ex);
        }
    }

    public void initialize() throws Exception {
        connectionFactory = new PooledConnectionFactory(new ActiveMQConnectionFactory(GlobalConfig.getActiveMqUrl()));
        connectionFactory.setMaxConnections(1);
        connectionFactory.setMaximumActive(1);
        connectionFactory.start();

        pooledConnection=(PooledConnection)connectionFactory.createConnection();
        pooledConnection.start();

        session = (PooledSession)pooledConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        session.setIgnoreClose(false);

        dest = session.createTopic(Level2DataTopicName + MQ_RETROACTIVE);
        producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        //load and subcribe default security
        subscribeDefaultSecurity();
    }

    public void stop(){
        try {
            connectionFactory.stop();
        }catch (Exception ex){
            LogFactory.error("stop orderbook active mq error!", ex);
        }
    }

    public String getSymbolByHsName(String hsName){
        if(hs_name_to_symbol_dic.keySet().contains(hsName))
            return hs_name_to_symbol_dic.get(hsName);
        return hsName;
    }

    public synchronized void subscribeOrderBook(String symbol,boolean forceToRefresh,OrderHandler handler){
        registerOrderBookHandlerMap(symbol, handler);
        if(securitiesSubscribed.containsKey(symbol)&& forceToRefresh==false){
            logger.info(String.format("Security %s has been subscribed and not set to refresh",symbol));
            return;
        }
        //SpinLock.Lock lock = spinLock.lock()
        try{
            session.close();
            session =(PooledSession)pooledConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);

            Security security = securitiesSubscribed.get(symbol);
            if(security==null){
                security = new Security(symbol,SecurityType.Stock);
                security.set_HSName("N/A");
            }

            securitiesSubscribed.putIfAbsent(symbol, security);
            logger.info(String.format("Security %s has been subscribed!",symbol));

            String filter = generateFilter();
            Destination dest = session.createTopic(Level2DataTopicName + MQ_RETROACTIVE);
            MessageConsumer consumer = session.createConsumer(dest, filter);

            //MessageConsumer consumer = session.createConsumer(dest);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        OrderBook ob = OrderBookParser.parse(text);
                        ob.setDateTime(LocalDateTime.now());
                        addNewOrderBook(ob);
                        broadcastOrderBookUpdatedEvent(ob);
                    } catch (Exception ex) {
                        LogFactory.error("parse market data mq text to order book error!", ex);
                    }
                }
            });
        }catch (Exception ex){
            String key = String.format(Alert.SUBCRIBE_MARKET_DATA_MQ_ERROR,symbol);
            String msg = ex.toString();
            Alert.fireAlert(Alert.Severity.Critical, key, msg, ex);
        }
    }

    @Override
    public String toString() {
        FormattedTable table = new FormattedTable();
        List<Object> row = new ArrayList<Object>();
        row.add("symbol");
        row.add("datetime");
        row.add("last px");
        row.add("pre close px");
        row.add("bp1");
        row.add("bs1");
        row.add("ap1");
        row.add("as1");
        row.add("bp2");
        row.add("bs2");
        row.add("ap2");
        row.add("as2");
        row.add("bp3");
        row.add("bs3");
        row.add("ap3");
        row.add("as3");
        table.AddRow(row);

        for (OrderBook ob: getLatestOrderBooks()){
            row = new ArrayList<Object>();
            row.add(ob.getSecurity().getSymbol());
            row.add(ob.getDateTime().toLocalTime().toString());
            row.add(Double.toString(ob.getLastPrice()) );
            row.add(Double.toString(ob.getPreClose()));

            row.add(Double.toString(ob.getBidPrice(0)));
            row.add(Double.toString(ob.getBidQty(0)));
            row.add(Double.toString(ob.getAskPrice(0)));
            row.add(Double.toString(ob.getAskQty(0)));

            row.add(Double.toString(ob.getBidPrice(1)));
            row.add(Double.toString(ob.getBidQty(1)));
            row.add(Double.toString(ob.getAskPrice(1)));
            row.add(Double.toString(ob.getAskQty(1)));

            row.add(Double.toString(ob.getBidPrice(2)));
            row.add(Double.toString(ob.getBidQty(2)));
            row.add(Double.toString(ob.getAskPrice(2)));
            row.add(Double.toString(ob.getAskQty(2)));
            table.AddRow(row);
        }

        return table.toString();
    }
}
