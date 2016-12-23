package com.csc108.decision;

import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.Allocation;
import com.csc108.model.fixModel.order.OrderHandler;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2016/12/23.
 */
public class FinisherDecision extends BaseDecision{

    @Override
    public boolean allocateDecision(OrderHandler orderHandler, Ref<Double> qtyToAllocate_, ArrayList<Allocation> allocationDecisions_, ArrayList<OmEvent> events, ArrayList<String> logLines) {
        return false;
    }
}
