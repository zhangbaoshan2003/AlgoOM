package tradingRule;
import static org.mockito.Mockito.*;

import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.RuleEngine;
import com.csc108.tradingRule.evaluators.AccountIDEvaluator;
import com.csc108.tradingRule.evaluators.AlwaysTrueEvaluator;
import com.csc108.tradingRule.evaluators.NumOfOrdersPerAccountEvaluator;
import com.csc108.tradingRule.handlers.AssembleDecisionChainHandler;
import com.csc108.tradingRule.handlers.RejectClientOrderHandler;
import com.csc108.tradingRule.providers.EvaluatorProvider;
import com.csc108.tradingRule.providers.HandlerProvider;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

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

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TradingRuleProvider.getInstance().initialize("NormalTradingRule.xml");
    }


    public void testEngineMockBehavior() throws Exception {
        int topNumOfOrdersAllowed=10;
        String accountId="123456789";
        ClientOrder clientOrder=new ClientOrder();
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);

        when(accountIDEvaluator.getEvaluatorName()).thenReturn("AccountIDEvaluator");
        when(accountIDEvaluator.evaluate(orderHandler, "123456789")).thenReturn(true);

        when(numOfOrdersPerAccountEvaluator.getEvaluatorName()).thenReturn("NumOfOrdersPerAccountEvaluator");
        when(numOfOrdersPerAccountEvaluator.evaluate(orderHandler, String.format("%s:%d", accountId, topNumOfOrdersAllowed)))
                .thenReturn(true);

        when(alwaysTrueEvaluator.getEvaluatorName()).thenReturn("AlwaysTrueEvaluator");
        //when(alwaysTrueEvaluator.evaluate(orderHandler, anyString())).thenReturn(true);

        when(rejectClientOrderHandler.getHandlerName()).thenReturn("RejectClientOrderHandler");
        when(assembleDecisionChainHandler.getHandlerName()).thenReturn("AssembleDecisionChainHandler");

        EvaluatorProvider.getEvaluators().clear();
        EvaluatorProvider.getEvaluators().put(accountIDEvaluator.getEvaluatorName(), accountIDEvaluator);
        EvaluatorProvider.getEvaluators().put(numOfOrdersPerAccountEvaluator.getEvaluatorName(), numOfOrdersPerAccountEvaluator);
        EvaluatorProvider.getEvaluators().put(alwaysTrueEvaluator.getEvaluatorName(), alwaysTrueEvaluator);


        HandlerProvider.getHandlers().clear();
        HandlerProvider.getHandlers().put(rejectClientOrderHandler.getHandlerName(), rejectClientOrderHandler);
        HandlerProvider.getHandlers().put(assembleDecisionChainHandler.getHandlerName(), assembleDecisionChainHandler);

        RuleEngine.process(orderHandler);
        LinkedHashMap<String,String> paramMap = new LinkedHashMap<>();
        verify(rejectClientOrderHandler,times(1)).handle(orderHandler,paramMap);

        orderHandler = new OrderHandler(clientOrder,null);
        clientOrder.setAccountId("0000");
        try{
            RuleEngine.process(orderHandler);
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
        }

        verify(rejectClientOrderHandler,never()).handle(orderHandler, paramMap);
    }


}
