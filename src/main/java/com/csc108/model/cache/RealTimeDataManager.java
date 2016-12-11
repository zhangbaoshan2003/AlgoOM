package com.csc108.model.cache;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.EventType;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnection;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnectionFactory;
import com.csc108.infrastructure.pooledActiveMQ.PooledSession;
import com.csc108.log.LogFactory;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.market.AllDayIntervalMarketData;
import com.csc108.model.market.BaseMarketData;
import com.csc108.model.market.IntervalMarketData;
import com.csc108.model.market.RealTimeMarketData;
import com.csc108.utility.DateTimeUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;

import javax.jms.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by LEGEN on 2016/7/15.
 */
public class RealTimeDataManager {
    private final String DATA_CALC_FORMAT="DATA_CALC_%s_%s_1_rt";
    private final String MQ_REQUEST = "ENGINE.REQUEST.CALC";
    private final String MQ_DATA_CALC_PREFIX = "DATA_CALC_";



    private final String MQ_RETROACTIVE = "?consumer.retroactive=true";
    private final HashMap<String,HashMap<String,String>> topicSubscribed= new HashMap<>();

    private final HashMap<String,ArrayList<OrderHandler>>
            realTimeHandlersSubscribed = new HashMap<>();

    private final HashMap<String,ArrayList<OrderHandler>>
            intervalDataHandlersSubscribed = new HashMap<String,ArrayList<OrderHandler>>();

    private PooledConnectionFactory connectionFactory ;
    private PooledConnection pooledConnection;

    private final int maxNumOfSessions = 200;

    private static RealTimeDataManager instance  = new RealTimeDataManager();

    private void parseMarketData(HashMap<String,String> keyValues,BaseMarketData marketData){
        String patternStr="^\\d+.\\d+";
        Pattern pattern =Pattern.compile(patternStr);
        marketData.setDateTime(LocalDateTime.now());

        keyValues.forEach((k,v)->{
            if(pattern.matcher(v).find()){
                switch (k){
                    case  "vwp":
                        marketData.setVwp(Double.parseDouble(v));
                        break;

                    case  "vwpvs":
                        marketData.setVwpvs(Double.parseDouble(v));
                        break;

                    case  "hp":
                        marketData.setHp(Double.parseDouble(v));
                        break;

                    case  "indauc":
                        marketData.setIndauc(Double.parseDouble(v));
                        break;

                    case  "indaucs":
                        marketData.setIndaucs(Double.parseDouble(v));
                        break;

                    case  "op":
                        marketData.setOp(Double.parseDouble(v));
                        break;

                    case  "lu":
                        marketData.setLu(Double.parseDouble(v));
                        break;

                    case  "ld":
                        marketData.setLd(Double.parseDouble(v));
                        break;

                    case  "tp":
                        marketData.setTp(Double.parseDouble(v));
                        break;

                    case  "lp":
                        marketData.setLp(Double.parseDouble(v));
                        break;
                }
            }

        });
    }

    public static RealTimeDataManager getInstance(){
        return instance;
    }

    //only called by test
    public String buildMsgRequestKey(OrderHandler handler, boolean allDay,double maxSize,double minSize){
        //DATA_CALC_600000_sh_[excAMAuction=false]_20160818-09:15:00_20160818-15:00:00_interval
        //return "DATA_CALC_"+handler.getClientOrder().getSymbol()+"_sh_[excAMAuction=false]_20160819-09:15:00_20160819-15:00:00_interval";

        //DATA_CALC_600570_sh_[price=61.39:excAMAuction=false]_20160819-09:15:00_20160819-15:00:00_interval
        String starTime = DateTimeUtil.allDayStartTradingTime();

        String endTime= DateTimeUtil.allDayEndTradingTime();

        if(handler==null || handler.getClientOrder()==null)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(MQ_DATA_CALC_PREFIX).append(handler.getClientOrder().getSymbol())
                .append("_")
                .append(handler.getClientOrder().getExchangeDest())
                .append("_[");
        if(handler.getClientOrder().getOrderType().equals("LIMIT")){
            sb.append("price=").append(handler.getClientOrder().getPrice()).append(":");
        }

        if(allDay){
            sb.append("excAMAuction=false:");
        }else{
            sb.append("excAMAuction=true:");
        }

        if(maxSize!=-1.0){
            sb.append("excLargeSize=").append(maxSize).append(":");
        }
        if(minSize!=-1.0){
            sb.append("excSmallSize=").append(minSize).append(":");
        }
        sb.replace(sb.length() - 1, sb.length(), "").append("]_");

        if(allDay==true){

        }else{
            starTime = DateTimeUtil.dateTime2Str(handler.getClientOrder().getEffectiveTime());
            endTime = DateTimeUtil.dateTime2Str(handler.getClientOrder().getExpireTime());
        }

        sb.append(starTime).append("_").append(endTime).append("_interval");

        return sb.toString();
    }

    public void initialize() throws Exception {
        connectionFactory = new PooledConnectionFactory(new ActiveMQConnectionFactory(GlobalConfig.getRealTimeDataMqUrl()));
        connectionFactory.setMaxConnections(1);
        connectionFactory.setMaximumActive(maxNumOfSessions);
        connectionFactory.start();

        pooledConnection=(PooledConnection)connectionFactory.createConnection();
        pooledConnection.start();

        //warm up sesion
        for (int i=0;i<maxNumOfSessions;i++){
            PooledSession session  = (PooledSession)pooledConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            session.setIgnoreClose(true);
            session.close();
        }
    }

    public void stop(){
        try{
            connectionFactory.stop();
        }catch (Exception ex){
            LogFactory.error("Close relatime data mq factory error!",ex);
        }
    }

    private void registerCalcRequest(HashMap<String,String> requests) throws Exception {
        try{
            String queueName = MQ_REQUEST;
            PooledSession session =(PooledSession)pooledConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination dest = session.createQueue(queueName + MQ_RETROACTIVE);
            MapMessage msgMap=session.createMapMessage();
            MessageProducer producer = session.createProducer(dest);
            msgMap.setString("Instance",GlobalConfig.getMonitorIP()+":"+GlobalConfig.getMonitorPort());

            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            requests.forEach((x,y)->{
                try{
                    msgMap.setString(x,y);
                }catch (Exception ex){
                    LogFactory.error("build real time calc request error!",ex);
                }
            });

            producer.send(dest, msgMap);
            session.close();

        }catch (Exception ex){
            LogFactory.error("register real time calc error!",ex);
        }
    }

    private void subscribeMarketData(OrderHandler orderHandler, boolean forceToRefresh,boolean isAllDayInterval,boolean isRealTime,boolean isInterval){
        try{
            if(orderHandler==null)
                return;

            ArrayList<OrderHandler>  handlers = new ArrayList<>();

            String topicName ="";

            String symbol = orderHandler.getClientOrder().getSymbol();
            String exDest = orderHandler.getClientOrder().getExchangeDest();
            if(isRealTime){
                topicName = String.format(DATA_CALC_FORMAT, symbol, exDest);
                if(realTimeHandlersSubscribed.keySet().contains(topicName)==false){
                    synchronized (realTimeHandlersSubscribed){
                        if(realTimeHandlersSubscribed.keySet().contains(topicName)==false){
                            HashMap<String,String> reuqests = new HashMap<>();
                            reuqests.putIfAbsent("topic", topicName);
                            registerCalcRequest(reuqests);
                            realTimeHandlersSubscribed.put(topicName,handlers);
                            handlers.add(orderHandler);
                        }
                    }
                }else{
                    synchronized (realTimeHandlersSubscribed){
                        handlers = realTimeHandlersSubscribed.get(topicName);
                        if(handlers.stream().filter(x->x.getClientOrder().getClientOrderId().
                                equals(orderHandler.getClientOrder().getClientOrderId())).findAny().isPresent()==false){
                            handlers.add(orderHandler);
                        }
                    }
                }
            }
            else{
                topicName = buildMsgRequestKey(orderHandler, isAllDayInterval, -1, -1);

                if(intervalDataHandlersSubscribed.keySet().contains(topicName)==false){
                    synchronized (intervalDataHandlersSubscribed){
                        if(intervalDataHandlersSubscribed.keySet().contains(topicName)==false){
                            HashMap<String,String> reuqests = new HashMap<>();
                            reuqests.putIfAbsent("topic",topicName);
                            registerCalcRequest(reuqests);
                            handlers = new ArrayList<>();
                            handlers.add(orderHandler);
                            intervalDataHandlersSubscribed.put(topicName,handlers);
                        }
                    }
                }else{
                    synchronized (intervalDataHandlersSubscribed){
                        handlers = intervalDataHandlersSubscribed.get(topicName);
                        if(handlers.stream().filter(x->x.getClientOrder().getClientOrderId().
                                equals(orderHandler.getClientOrder().getClientOrderId())).findAny().isPresent()==false){
                            handlers.add(orderHandler);
                        }
                    }
                }
            }

            //register handler
            PooledSession session =(PooledSession)pooledConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination dest = session.createTopic(topicName + MQ_RETROACTIVE);
            MessageConsumer consumer = session.createConsumer(dest);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        ActiveMQMapMessage msg = (ActiveMQMapMessage) message;
                        if (msg == null) {
                            LogFactory.info("Empty real time data msg received");
                            return;
                        }

                        HashMap<String, String> dValue = new HashMap<String, String>();

                        Enumeration mapNames = msg.getMapNames();
                        while (mapNames.hasMoreElements()) {
                            String name = (String) mapNames.nextElement();
                            String value = msg.getString(name.toString());
                            dValue.put(name, value);
                            //System.out.printf("RealTime name:%s value:%s @ thread %s \n", name, value, Thread.currentThread().getId());
                        }
                        //topicSubscribed.put(topicName,dValue);

                        ArrayList<OrderHandler> handlersSubscrribed = null;
                        if (isRealTime) {
                            String topicName = String.format(DATA_CALC_FORMAT, symbol, exDest);
                            handlersSubscrribed = realTimeHandlersSubscribed.get(topicName);
                            synchronized (handlersSubscrribed){
                                try{
                                    if(handlersSubscrribed!=null && handlersSubscrribed.size()>0){
                                        BaseMarketData mktData = new RealTimeMarketData();
                                        parseMarketData(dValue, mktData);
                                        handlersSubscrribed.forEach(handler -> {
                                            DisruptorController controller = handler.getController();
                                            controller.enqueueEvent(EventType.MARKET_DATA_UPDATED, handler, mktData);
                                        });
                                    }
                                }catch (Exception ex){
                                    LogFactory.error("Parse real time data error!",ex);
                                }
                            }


                        } else if (isAllDayInterval) {
                            String topicName = buildMsgRequestKey(orderHandler, isAllDayInterval, -1, -1);
                            handlersSubscrribed = intervalDataHandlersSubscribed.get(topicName);

                            synchronized (handlersSubscrribed){
                                try {
                                    if(handlersSubscrribed!=null && handlersSubscrribed.size()>0){
                                        BaseMarketData mktData = new AllDayIntervalMarketData();
                                        parseMarketData(dValue, mktData);
                                        handlersSubscrribed.forEach(handler -> {
                                            DisruptorController controller = handler.getController();
                                            controller.enqueueEvent(EventType.MARKET_DATA_UPDATED, handler, mktData);
                                        });
                                    }
                                }catch (Exception ex){
                                    LogFactory.error("Parse full day market data error!",ex);
                                }
                            }


                        } else {
                            String topicName = buildMsgRequestKey(orderHandler, isAllDayInterval, -1, -1);
                            handlersSubscrribed = intervalDataHandlersSubscribed.get(topicName);
                            synchronized (handlersSubscrribed ){
                                try{
                                    if(handlersSubscrribed!=null && handlersSubscrribed.size()>0){
                                        BaseMarketData mktData = new IntervalMarketData();
                                        parseMarketData(dValue, mktData);
                                        handlersSubscrribed.forEach(handler -> {
                                            DisruptorController controller = handler.getController();
                                            controller.enqueueEvent(EventType.MARKET_DATA_UPDATED, handler, mktData);
                                        });
                                    }

                                }catch (Exception ex){
                                    LogFactory.error("Parse interval market data error!",ex);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        LogFactory.error("Parse market time data error!", ex);
                    }
                }
            });

            //return session cached to pool
            session.close();

        }catch (Exception ex){
            LogFactory.error("subscribe realtime market data error!",ex);
        }
    }

    public void subscribeRealTimeData(OrderHandler orderHandler,boolean forceToRefresh){
        subscribeMarketData(orderHandler,forceToRefresh,false,true,false);
    }

    public void subscribeIntervalData(OrderHandler orderHandler,boolean forceToRefresh){
        subscribeMarketData(orderHandler,forceToRefresh,false,false,true);
    }

    public void subscribeAlldayIntervalData(OrderHandler orderHandler,boolean forceToRefresh){
        subscribeMarketData(orderHandler,forceToRefresh,true,false,false);
    }
}
