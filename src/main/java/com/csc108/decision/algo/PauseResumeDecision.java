package com.csc108.decision.algo;

import com.csc108.configuration.GlobalConfig;
import com.csc108.decision.BaseDecision;
import com.csc108.decision.configuration.Ref;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.Allocation;
import com.csc108.model.PauseResumeEvaluationData;
import com.csc108.model.WakeupEvaluationData;
import com.csc108.model.criteria.Condition;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.utility.Alert;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2016/6/3.
 */
public class PauseResumeDecision extends BaseDecision {

    @Override
    public String toString(){
        return "PauseResumeDecision";
    }

    @Override
    public boolean allocateDecision(OrderHandler orderHandler, Ref<Double> qtyToAllocate_,
                                    ArrayList<Allocation> allocationDecisions_,
                                    ArrayList<OmEvent> events,  ArrayList<String> logLines) {

        if(orderHandler.isConditionalOrder()==true ){
            Condition condition = orderHandler.getCondition();
            if(condition==null){
                if(condition==null){
                    throw new IllegalArgumentException("No condition is buit for the conditional order"+orderHandler.getClientOrder().getClientOrderId());
                }
            }else{
                try{
                    TradeAction tradeAction = condition.evaluate();

                    if(tradeAction==TradeAction.Pause){
                        if(orderHandler.isResumeByUser()){
                            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.PAUER_ERROR,orderHandler.getClientOrder().getClientOrderId()),
                                    "Order failed to be paused due to user resume behavior",null);
                            return false;
                        }else{
                            Alert.clearAlert(String.format(Alert.PAUER_ERROR,orderHandler.getClientOrder().getClientOrderId()));
                        }
                    }

                    if(tradeAction==TradeAction.Resume){
                        if(orderHandler.isPauseByUser()){
                            Alert.fireAlert(Alert.Severity.Info,String.format(Alert.RESUME_ERROR,orderHandler.getClientOrder().getClientOrderId()),
                                    "Order failed to be resumed due to user pause behavior",null);
                            return false;
                        }else{
                            Alert.clearAlert(String.format(Alert.RESUME_ERROR,orderHandler.getClientOrder().getClientOrderId()));
                        }
                    }

                    OmEvent pauseResumeEvent= new OmEvent();
                    pauseResumeEvent.setEventType(EventType.PAUSE_RESUME);
                    pauseResumeEvent.setDataHandler(orderHandler);
                    PauseResumeEvaluationData pauseResumeEvaluationData = new
                            PauseResumeEvaluationData(tradeAction,false);
                    pauseResumeEvent.setTriggerData(pauseResumeEvaluationData);
                    events.add(pauseResumeEvent);

//                    //schedule a wake up event to evaluate at a fix interval
//                    OmEvent wakeupEvent = new OmEvent();
//                    wakeupEvent.setTriggerData(new WakeupEvaluationData(System.currentTimeMillis() + GlobalConfig.getWakeupInterval() * 1000));
//                    wakeupEvent.setEventType(EventType.WAKEUP);
//                    wakeupEvent.setDataHandler(orderHandler);
//                    events.add(wakeupEvent );

                    return true;
                }catch (Exception ex){
                    Alert.fireAlert(Alert.Severity.Major, String.format(Alert.PROCESS_ORDER_ERROR, orderHandler.getClientOrder().getClientOrderId()),
                            "Evaluate the condition error", ex);

                    OmEvent wakeupEvent = new OmEvent();
                    wakeupEvent.setTriggerData(new WakeupEvaluationData(System.currentTimeMillis()+GlobalConfig.getWakeupInterval()*1000));
                    wakeupEvent.setEventType(EventType.WAKEUP);
                    wakeupEvent.setDataHandler(orderHandler);
                    events.add(wakeupEvent);

                    return false;
                }

//                OrderBook referenceOrderbook =  OmDataCacheManager.getInstance().getLatestOrderBooks().get(condition.getReferSecurity());
//                if(referenceOrderbook!=null){
//
//                }else {
//                    Alert.fireAlert(Alert.Severity.Major,String.format(Alert.PROCESS_ORDER_ERROR,orderHandler.getClientOrder().getClientOrderId()),
//                            "No market data to evaluate the condition",null);
//                    OmEvent wakeupEvent = new OmEvent();
//                    wakeupEvent.setTriggerData(new WakeupEvaluationData(System.currentTimeMillis()+GlobalConfig.getWakeupInterval()*1000));
//                    wakeupEvent.setEventType(EventType.WAKEUP);
//                    wakeupEvent.setDataHandler(orderHandler);
//                    events.add(wakeupEvent );
//                    return false;
//                }
            }
        }

        return true;

    }
}
