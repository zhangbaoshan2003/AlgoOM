package com.csc108.decision;

import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.Allocation;
import com.csc108.model.fixModel.order.OrderHandler;

import java.util.ArrayList;

/**
 * Created by LEGEN on 2016/5/2.
 */
public abstract class BaseDecision implements IDecision {

    @Override
    public abstract boolean allocateDecision(OrderHandler orderHandler,
                                             Ref<Double> qtyToAllocate_,
                                             ArrayList<Allocation> allocationDecisions_,
                                             ArrayList<OmEvent> events,  ArrayList<String> logLines);
}
