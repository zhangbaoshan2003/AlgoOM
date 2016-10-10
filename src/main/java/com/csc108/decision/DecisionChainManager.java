package com.csc108.decision;

import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.exceptions.OverAllocateException;
import com.csc108.model.Allocation;
import com.csc108.model.fix.order.OrderHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/5/12.
 */
public class DecisionChainManager {
    private final List<BaseDecision> decisions;
    public List<BaseDecision> getDecisions(){
        return decisions;
    }

    public DecisionChainManager(List<BaseDecision> local_decisions){
        decisions = local_decisions;
    }

    public void invokeDecision(OrderHandler clientOrderManager,ArrayList<Allocation> allocations,
                               ArrayList<OmEvent> events,  ArrayList<String> logLines) throws Exception {
        Ref<Double> quantityToAllocate=
                //new Ref<Double>(clientOrderManager.getClientOrder().getOrderQty());
                new Ref<Double>(clientOrderManager.getClientOrder().getLeavesQty());

        for(IDecision decision : decisions){
            decision.allocateDecision(clientOrderManager,quantityToAllocate,allocations,events,logLines);
        }
    }

}
