package tradingRule.handlers;

import com.csc108.decision.DecisionChainManager;
import com.csc108.decision.algo.DeliverToEngineDecision;
import com.csc108.decision.algo.PauseResumeDecision;
import com.csc108.decision.pegging.PeggingDecision;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.handlers.AssembleDecisionChainHandler;
import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by zhangbaoshan on 2016/11/3.
 */
public class AssembleDescisonHandlerTest extends TestCase {

    public void testAssembDecisionHandler(){
        AssembleDecisionChainHandler handler = new AssembleDecisionChainHandler();

        ClientOrder clientOrder  =new ClientOrder();
        OrderHandler orderHandler= new OrderHandler(clientOrder,null);

        boolean expectedException=false;
        try {
            handler.handle(orderHandler,new LinkedHashMap<>());
        }catch (Exception ex){
            expectedException=true;
            System.out.println(ex.getMessage());
        }

        assertEquals(true, expectedException);

        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("Para1","PeggingDecision");
        handler.handle(orderHandler, params);
        DecisionChainManager decisionChainManager = orderHandler.getDecisionChain();
        assertEquals(1,decisionChainManager.getDecisions().size());
        assertEquals(PeggingDecision.class,decisionChainManager.getDecisions().get(0).getClass());

        params = new LinkedHashMap<>();
        params.put("Para1", "DeliverToEngineDecision");
        handler.handle(orderHandler, params);
        decisionChainManager = orderHandler.getDecisionChain();
        assertEquals(1,decisionChainManager.getDecisions().size());
        assertEquals(DeliverToEngineDecision.class,decisionChainManager.getDecisions().get(0).getClass());

        params = new LinkedHashMap<>();
        params.put("Para1", "PauseResumeDecision");
        handler.handle(orderHandler, params);
        decisionChainManager = orderHandler.getDecisionChain();
        assertEquals(1,decisionChainManager.getDecisions().size());
        assertEquals(PauseResumeDecision.class,decisionChainManager.getDecisions().get(0).getClass());

        LinkedHashMap<String,String> paramLinked = new LinkedHashMap<>();
        paramLinked.put("Para1","PauseResumeDecision");
        paramLinked.put("Para2","PeggingDecision");
        paramLinked.put("Para3","DeliverToEngineDecision");

        params.keySet().stream().sorted();

        handler.handle(orderHandler, paramLinked);
        decisionChainManager = orderHandler.getDecisionChain();
        assertEquals(3,decisionChainManager.getDecisions().size());
        assertEquals(PauseResumeDecision.class,decisionChainManager.getDecisions().get(0).getClass());
        assertEquals(PeggingDecision.class,decisionChainManager.getDecisions().get(1).getClass());
        assertEquals(DeliverToEngineDecision.class,decisionChainManager.getDecisions().get(2).getClass());
    }
}
