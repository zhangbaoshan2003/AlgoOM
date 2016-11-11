package tradingRule;
import static org.mockito.Mockito.*;

import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.fix.order.OrderPool;
import com.csc108.tradingRule.RuleEngine;
import com.csc108.tradingRule.evaluators.AccountIDEvaluator;
import com.csc108.tradingRule.evaluators.AlwaysTrueEvaluator;
import com.csc108.tradingRule.evaluators.NumOfOrdersPerAccountEvaluator;
import com.csc108.tradingRule.handlers.AssembleDecisionChainHandler;
import com.csc108.tradingRule.handlers.CallingOrderHandlerProcessHandler;
import com.csc108.tradingRule.handlers.RejectClientOrderHandler;
import com.csc108.tradingRule.providers.EvaluatorProvider;
import com.csc108.tradingRule.providers.HandlerProvider;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import utility.TestCaseBase;
import utility.TestFixMsgHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class RuleEngineTest extends TestCase {

    @Mock private AccountIDEvaluator accountIDEvaluator;
    @Mock private NumOfOrdersPerAccountEvaluator numOfOrdersPerAccountEvaluator;
    @Mock private AlwaysTrueEvaluator alwaysTrueEvaluator;

    @Mock private RejectClientOrderHandler rejectClientOrderHandler;
    @Mock private AssembleDecisionChainHandler assembleDecisionChainHandler;
    @Mock private CallingOrderHandlerProcessHandler callingOrderHandlerProcessHandler;

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private void prepareMockProviders_For_MaximumOrdersRule(OrderHandler orderHandler) throws Exception {
        int topNumOfOrdersAllowed=10;
        String accountId="123456789";

        when(accountIDEvaluator.getEvaluatorName()).thenReturn("AccountIDEvaluator");
        when(accountIDEvaluator.evaluate(orderHandler, "123456789")).thenReturn(true);

        when(numOfOrdersPerAccountEvaluator.getEvaluatorName()).thenReturn("NumOfOrdersPerAccountEvaluator");
        when(numOfOrdersPerAccountEvaluator.evaluate(orderHandler, String.format("%s:%d", accountId, topNumOfOrdersAllowed)))
                .thenReturn(true);

        when(alwaysTrueEvaluator.getEvaluatorName()).thenReturn("AlwaysTrueEvaluator");
        //when(alwaysTrueEvaluator.evaluate(orderHandler, anyString())).thenReturn(true);

        when(rejectClientOrderHandler.getHandlerName()).thenReturn("RejectClientOrderHandler");
        when(assembleDecisionChainHandler.getHandlerName()).thenReturn("AssembleDecisionChainHandler");
        when(callingOrderHandlerProcessHandler.getHandlerName()).thenReturn("CallingOrderHandlerProcessHandler");

        EvaluatorProvider.getEvaluators().clear();
        EvaluatorProvider.getEvaluators().put(accountIDEvaluator.getEvaluatorName(), accountIDEvaluator);
        EvaluatorProvider.getEvaluators().put(numOfOrdersPerAccountEvaluator.getEvaluatorName(), numOfOrdersPerAccountEvaluator);
        EvaluatorProvider.getEvaluators().put(alwaysTrueEvaluator.getEvaluatorName(), alwaysTrueEvaluator);


        HandlerProvider.getHandlers().clear();
        HandlerProvider.getHandlers().put(rejectClientOrderHandler.getHandlerName(), rejectClientOrderHandler);
        HandlerProvider.getHandlers().put(assembleDecisionChainHandler.getHandlerName(), assembleDecisionChainHandler);
        HandlerProvider.getHandlers().put(callingOrderHandlerProcessHandler.getHandlerName(), callingOrderHandlerProcessHandler);
    }


    public void testRejectOrderDueToExceedLimitRule() throws Exception {

        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                1000,10.2);
        ClientOrder clientOrder=new ClientOrder(newOrderSingle,null);
        clientOrder.setAccountId("123456789");
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);

        //prepareMockProviders_For_MaximumOrdersRule(orderHandler);
        TradingRuleProvider.getInstance().initialize("NormalTradingRule.xml");
        LinkedHashMap<String,String> paramMap = new LinkedHashMap<>();

        //simulate accout 123456789 sent out 9 orders
        for (int i=0;i<10;i++){
            newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                    1000,10.2);
            ClientOrder newOrder = new ClientOrder(newOrderSingle,null);
            newOrder.setAccountId("123456789");
            OrderPool.getClientOrderMap().put(newOrder.getClientOrderId(),newOrder);
        }
        RuleEngine.process(orderHandler);

        newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                1000,10.2);
        ClientOrder newOrder = new ClientOrder(newOrderSingle,null);
        newOrder.setAccountId("123456789");
        OrderPool.getClientOrderMap().put(newOrder.getClientOrderId(), newOrder);
        RuleEngine.process(orderHandler);
    }
}
