package com.csc108.tradingRule.handlers;

import com.csc108.decision.BaseDecision;
import com.csc108.decision.DecisionChainManager;
import com.csc108.decision.algo.DeliverToEngineDecision;
import com.csc108.decision.algo.PauseResumeDecision;
import com.csc108.decision.pegging.PeggingDecision;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.utility.Alert;

import java.util.*;

/**
 * Created by zhangbaoshan on 2016/11/2.
 */
public class AssembleDecisionChainHandler implements IHandler {

    @Override
    public String getHandlerName() {
        return "AssembleDecisionChainHandler";
    }

    @Override
    public void handle(OrderHandler orderHandler, LinkedHashMap<String, String> parameters) {
        ArrayList<BaseDecision> decisions = new ArrayList<>();

        Iterator<String> decisionIterator =parameters.values().iterator();

        while (decisionIterator.hasNext()){
            String decisionName = decisionIterator.next();
            if(decisionName.equals("PeggingDecision")){
                decisions.add(new PeggingDecision());
            }
            if(decisionName.equals("DeliverToEngineDecision")){
                decisions.add(new DeliverToEngineDecision());
            }
            if(decisionName.equals("PauseResumeDecision")){
                decisions.add(new PauseResumeDecision());
            }
        }

        if(decisions.size()==0){
            throw new IllegalArgumentException("No decision made! @ "+orderHandler.getClientOrder().getClientOrderId());
        }

        DecisionChainManager manager = new DecisionChainManager(decisions);
        orderHandler.setDecisionChain(manager);
    }
}
