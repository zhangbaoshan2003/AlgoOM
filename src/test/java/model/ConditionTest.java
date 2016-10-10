package model;

import com.csc108.log.LogFactory;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.criteria.Condition;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import com.csc108.model.market.OrderBook;
import com.csc108.model.market.Quote;
import junit.framework.TestCase;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import utility.TestFixMsgHelper;
import utility.TestUtility;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by LEGEN on 2016/6/5.
 */
public class ConditionTest extends TestCase {

    private OrderBook buildOrderBook(String symbol) {
        List<Quote> ask = new ArrayList<Quote>();
        ask.add(new Quote(10.0, 1000));
        ask.add(new Quote(10.1, 2000));
        ask.add(new Quote(10.2, 3000));

        List<Quote> bid = new ArrayList<Quote>();
        bid.add(new Quote(9.9, 4000));
        bid.add(new Quote(9.8, 5000));
        bid.add(new Quote(9.7, 6000));

        OrderBook book = OrderBook.of();
        book.setAsk(ask);
        book.setBid(bid);

        assertEquals(9.9, book.getBidPrice(0), TestUtility.Delta);
        assertEquals(9.8, book.getBidPrice(1), TestUtility.Delta);
        book.setPreClose(10.0);
        book.setHighestPrice(11.0);
        book.setLastPrice(9.0);
        book.setOpenPrice(9.2);
        book.setSecurity(new Security(symbol, SecurityType.Stock));

        assertEquals(Double.MIN_VALUE, book.getBidPrice(10), TestUtility.Delta);
        assertEquals(Double.MIN_VALUE, book.getBidPrice(-1), TestUtility.Delta);
        return book;
    }


    public void testCondition() throws Exception {
        OrderbookDataManager.getInstance().initialize();
        //TimeUnit.SECONDS.sleep(3);

        //no condition related tag, no condition built
        //String referIndex="000016.sh";
        String referIndex=".CSI300";


        NewOrderSingle orderNewSingle = TestFixMsgHelper.Instance.buildNewOrderSingleMsg
                ("IBM", new Side(Side.BUY), new OrdType(OrdType.MARKET), 1000, 98.00);
        Condition condition=null;
        try{
            condition = Condition.build(orderNewSingle,null);
        }catch (Exception ex){
            LogFactory.error(ex.getMessage(),ex);
        }

        assertNull(condition);

        //no market data, condition can be built, but no market value
        orderNewSingle.setString(Condition.TAG_REFER_SECURITY, referIndex);
        orderNewSingle.setInt(Condition.TAG_OP, 0);
        orderNewSingle.setInt(Condition.TAG_RELATIVE_VALUE, 5);
        orderNewSingle.setInt(Condition.TAG_ACTION, 1);
        orderNewSingle.setInt(Condition.TAG_BENCHMARK, 10);

        try{
            condition = Condition.build(orderNewSingle,null);
        }catch (Exception ex){
            LogFactory.error(ex.getMessage(),ex);
        }
        assertNotNull(condition);

        //evaluate condition: when current last px>preclose*(1+0.05), trigger pause, or else resume
        String suymbolRefered = OrderbookDataManager.getInstance().getSymbolByHsName(referIndex);
        OrderBook latestOrderbook = buildOrderBook(suymbolRefered);

        OrderbookDataManager.getInstance().addNewOrderBook(latestOrderbook);

        TradeAction tradeAction= condition.evaluate();
        assertEquals(TradeAction.Resume,tradeAction);
        System.out.println(condition);

        //OmDataCacheManager.getInstance().getLatestOrderBooks().put(referIndex, referOrderBook);

        orderNewSingle.removeField(Condition.TAG_RELATIVE_VALUE);
        orderNewSingle.setInt(Condition.TAG_ABS_VALUE, 10);

        try{
            condition = Condition.build(orderNewSingle,null);
        }catch (Exception ex){
            LogFactory.error(ex.getMessage(),ex);
        }
        assertNotNull(condition);

        //OmDataCacheManager.getInstance().getLatestOrderBooks().put("000300.SH",latestOrderbook);
        latestOrderbook.setLastPrice(11);
        tradeAction = condition.evaluate();
        assertEquals(TradeAction.Pause, tradeAction);
        System.out.println(condition);

        latestOrderbook.setLastPrice(9);
        tradeAction = condition.evaluate();
        assertEquals(TradeAction.Resume, tradeAction);
        System.out.println(condition);

        //verify relative comarions
        orderNewSingle.removeField(Condition.TAG_ABS_VALUE);
        orderNewSingle.setInt(Condition.TAG_RELATIVE_VALUE, 10);
        try{
            condition = Condition.build(orderNewSingle,null);
        }catch (Exception ex){
            LogFactory.error(ex.getMessage(),ex);
        }
        assertNotNull(condition);

        latestOrderbook.setLastPrice(12);
        tradeAction = condition.evaluate();
        assertEquals(TradeAction.Pause, tradeAction);
        System.out.println(condition);

        latestOrderbook.setLastPrice(10);
        tradeAction = condition.evaluate();
        assertEquals(TradeAction.Resume, tradeAction);
        System.out.println(condition);
    }
}
