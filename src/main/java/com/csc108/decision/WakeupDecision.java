package com.csc108.decision;

import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.Allocation;
import com.csc108.model.fix.order.OrderHandler;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2016/6/2.
 */
public class WakeupDecision extends BaseDecision {

    @Override
    public String toString() {
        return "WakeupDecision";
    }

    @Override
    public boolean allocateDecision(OrderHandler orderHandler, Ref<Double> qtyToAllocate_,
                                    ArrayList<Allocation> allocationDecisions_,
                                    ArrayList<OmEvent> events , ArrayList<String> logLines){

        return true;
    }
}
