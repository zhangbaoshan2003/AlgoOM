package com.csc108.decision.algo;

import com.csc108.decision.BaseDecision;
import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.Allocation;
import com.csc108.model.fixModel.order.OrderHandler;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2016/8/2.
 */
public class DeliverToEngineDecision extends BaseDecision {
    @Override
    public String toString() {
        return "DeliverToEngineDecision";
    }

    @Override
    public boolean allocateDecision(OrderHandler orderHandler, Ref<Double> qtyToAllocate_,
                                    ArrayList<Allocation> allocationDecisions_,
                                    ArrayList<OmEvent> events,  ArrayList<String> logLines) {
        if(orderHandler.getExchangeOrders()!=null){
            //to see if all client order has been allocated
            double totalQtySentByExchangeOrders = orderHandler.getExchangeOrders().stream().mapToDouble(x->x.getOrderQty()).sum();
            if(Double.compare(totalQtySentByExchangeOrders,orderHandler.getClientOrder().getOrderQty())==0){
                allocationDecisions_.clear();
                //don't generate exchange order more than one time
                return true;
            }
        }

        //set up allocation
        Allocation allocation = new Allocation();
        allocation.setAllocatedQuantity(qtyToAllocate_.getValue());
        allocation.setAllocatedPrice(orderHandler.getClientOrder().getPrice());
        allocationDecisions_.add(allocation);
        double qtyLeft = qtyToAllocate_.getValue() - allocation.getAllocatedQuantity();
        qtyToAllocate_.setValue(qtyLeft);

        return true;
    }
}
