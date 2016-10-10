package com.csc108.decision;

import com.csc108.decision.algo.ConditionalOrderSentOutOrNotDecision;
import com.csc108.decision.algo.DeliverToEngineDecision;
import com.csc108.decision.algo.PauseResumeDecision;
import com.csc108.decision.pegging.PeggingDecision;
import com.csc108.model.fix.order.OrderHandler;
import quickfix.fix42.NewOrderSingle;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2016/8/2.
 */
public class DecisionMaker {

    public static DecisionChainManager build(OrderHandler handler){
        ArrayList<BaseDecision> decisions = new ArrayList<>();

        if(handler.isPeggingOrder()){
            decisions.add(new PeggingDecision());
        }else{
            decisions.add(new DeliverToEngineDecision());
        }

        if(handler.isConditionalOrder()){
            decisions.add(new PauseResumeDecision());
            decisions.add(new ConditionalOrderSentOutOrNotDecision());
        }

        DecisionChainManager manager = new DecisionChainManager(decisions);

        return manager;
    }
}
