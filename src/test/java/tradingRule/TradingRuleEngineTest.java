package tradingRule;

import com.csc108.model.cache.ReferenceDataManager;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.tradingRule.RuleEngine;
import com.csc108.tradingRule.core.IEvaluator;
import com.csc108.tradingRule.core.IRule;
import com.csc108.tradingRule.core.TradingRule;
import com.csc108.tradingRule.evaluators.ClientOrderTradingTimeValidEvaluator;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import com.csc108.utility.AlertManager;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import utility.TestCaseBase;
import utility.TestFixMsgHelper;
import utility.TestPurpose;
import utility.TestUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/11/10.
 */
public class TradingRuleEngineTest extends TestCaseBase {

    public void test001TopLimitOrdersExceedTradingRule() throws Exception {
        assertEquals(3, TradingRuleProvider.getInstance().getTradingRules().size());

        TestUtility.Purpose = TestPurpose.FULL_FILL;
        SessionID clientSessionId = clientApplication.getSessionID();
        assertEquals(true, Session.lookupSession(clientSessionId).isLoggedOn());

        //simulate sent out 10 orders, should all succeeded
        for (int i=0;i<10;i++){
            NewOrderSingle newOrderSingle = TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM", new Side(Side.BUY) ,new OrdType(OrdType.MARKET) ,10000,10.25);
            ClientOrder clientOrder = new ClientOrder(newOrderSingle,clientApplication.getSessionID());
            clientApplication.getOrderSet().putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
            Session.sendToTarget(newOrderSingle, clientSessionId);
            Thread.sleep(500);
            assertEquals(OrdStatus.FILLED, clientOrder.getOrdStatus().getValue());
        }

        //sent out the 11th order, should be rejected, due to the [MaximumOrdersRule]
        NewOrderSingle newOrderSingle = TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM", new Side(Side.BUY) ,new OrdType(OrdType.MARKET) ,10000,10.25);
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,clientApplication.getSessionID());
        clientApplication.getOrderSet().putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
        Session.sendToTarget(newOrderSingle, clientSessionId);
        Thread.sleep(500);
        assertEquals(OrdStatus.REJECTED, clientOrder.getOrdStatus().getValue());

        //change the rule at run time
        RuleEngine.updateRuleEvaluator("MaximumOrdersRule","NumOfOrdersPerAccountEvaluator","Acct_PB_01:11");

        //send out again, this time, shouldn't be rejected any longer
        newOrderSingle = TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM", new Side(Side.BUY) ,new OrdType(OrdType.MARKET) ,10000,10.25);
        clientOrder = new ClientOrder(newOrderSingle,clientApplication.getSessionID());
        clientApplication.getOrderSet().putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
        Session.sendToTarget(newOrderSingle, clientSessionId);
        Thread.sleep(500);
        assertEquals(OrdStatus.FILLED, clientOrder.getOrdStatus().getValue());

        //send out again, this time, should be rejected any longer
        newOrderSingle = TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM", new Side(Side.BUY) ,new OrdType(OrdType.MARKET) ,10000,10.25);
        clientOrder = new ClientOrder(newOrderSingle,clientApplication.getSessionID());
        clientApplication.getOrderSet().putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
        Session.sendToTarget(newOrderSingle, clientSessionId);
        Thread.sleep(500);
        assertEquals(OrdStatus.REJECTED, clientOrder.getOrdStatus().getValue());

    }

    public void test002OrderTradingTimeRule() throws Exception {
        CountDownLatch gate =new CountDownLatch(1);

        SessionID clientSessionId = clientApplication.getSessionID();
        AlertManager.getInstance().init();
        ReferenceDataManager.getInstance().init();
        TradingRuleProvider.getInstance().initialize("PeggingTradingRule.xml");

        ArrayList<IRule> rules = TradingRuleProvider.getInstance().getTradingRules();

        IEvaluator evaluator = new ClientOrderTradingTimeValidEvaluator();
        assertEquals("ClientOrderTradingTimeValidEvaluator",evaluator.getEvaluatorName());

        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-12:30:00","20161212-15:00:00");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,clientApplication.getSessionID());
        clientApplication.getOrderSet().putIfAbsent(clientOrder.getClientOrderId(), clientOrder);
        Session.sendToTarget(newOrderSingle, clientSessionId);

        TimeUnit.SECONDS.sleep(1);
        //gate.await();

        assertEquals(OrdStatus.REJECTED, clientOrder.getOrdStatus().getValue());
    }
}
