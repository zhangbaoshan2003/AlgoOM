package com.csc108.decision.algo;

import com.csc108.configuration.GlobalConfig;
import com.csc108.decision.BaseDecision;
import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.Allocation;
import com.csc108.model.WakeupEvaluationData;
import com.csc108.model.criteria.Condition;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.utility.Alert;
import quickfix.field.OrdStatus;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2016/7/22.
 */
public class ConditionalOrderSentOutOrNotDecision extends BaseDecision {

    @Override
    public String toString(){
        return "ConditionalOrderSentOutOrNotDecision";
    }

    @Override
    public boolean allocateDecision(OrderHandler orderHandler,
                                    Ref<Double> qtyToAllocate_, ArrayList<Allocation> allocationDecisions_,
                                    ArrayList<OmEvent> events,  ArrayList<String> logLines) {

        if(orderHandler.isConditionalOrder()==true ){
            Condition condition =orderHandler.getCondition();
            if(condition==null){
                //clear allocation to stop sending out exchange orders since no condition is built for the conditional order
                allocationDecisions_.clear();

                Alert.fireAlert(Alert.Severity.Major,String.format(Alert.PROCESS_CONDITION_ORDER_ERROR,orderHandler.getAlertID()),
                        String.format("No condition is built for the conditional order's {%s} ",orderHandler.getClientOrder().getSymbol()) ,null);

                //schedule a wake up event to evaluate at fixModel interval
                OmEvent wakeupEvent = new OmEvent();
                wakeupEvent.setTriggerData(new WakeupEvaluationData(System.currentTimeMillis() + GlobalConfig.getWakeupInterval() * 1000));
                wakeupEvent.setEventType(EventType.WAKEUP);
                wakeupEvent.setDataHandler(orderHandler);
                events.add(wakeupEvent );
                return false;
                //throw new IllegalArgumentException("No condition is buit for the conditional order"+orderHandler.getAlertID());
            }else{
                Alert.clearAlert(String.format(Alert.PROCESS_CONDITION_ORDER_ERROR, orderHandler.getAlertID()));
            }

            TradeAction tradeAction = condition.evaluate();
            if(tradeAction==TradeAction.Pause){
                //can't split and sent out when the order is new staus
                if(orderHandler.getClientOrder().getOrdStatus().getValue()== OrdStatus.PENDING_NEW
                        || orderHandler.getClientOrder().getOrdStatus().getValue()== OrdStatus.NEW){
                    allocationDecisions_.clear();
                    Alert.fireAlert(Alert.Severity.Info,String.format(Alert.PROCESS_CONDITION_ORDER_ERROR,orderHandler.getAlertID()),
                            String.format("Order for {%s} current market criteria meets pause condition {%s}",orderHandler.getClientOrder().getSymbol(),condition.toString()) ,null);
                }
                return false;
            }else{
                Alert.clearAlert(String.format(Alert.PROCESS_CONDITION_ORDER_ERROR, orderHandler.getAlertID()));
            }
        }

        return true;

    }
}
