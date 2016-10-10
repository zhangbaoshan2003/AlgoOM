package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.OrderState;
import com.csc108.model.PauseResumeEvaluationData;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.fix.order.OrderPool;
import com.csc108.model.market.OrderBook;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import sun.rmi.runtime.Log;

/**
 * Created by zhangbaoshan on 2016/6/1.
 */
public class PauseResumeEventHandler extends EventHandlerBase {

    public static PauseResumeEventHandler Instance= new PauseResumeEventHandler();

    private PauseResumeEventHandler(){

    }

    @Override
    public String doingWhat(OmEvent eventSource){
        PauseResumeEvaluationData pauseResumeEvaluationData = (PauseResumeEvaluationData)eventSource.getTriggerData();
        if(pauseResumeEvaluationData!=null){
            return pauseResumeEvaluationData.getTradeAction()==TradeAction.Pause?"PAUSE":"RESUME";
        }
        return "";
    }

    @Override
    public String getHandlerName(){
        return "PauseResumeEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){
        OrderHandler orderHandler =  (OrderHandler)eventSource.getDataHandler();
        try{
            if(FixUtil.IsOrderCompleted(orderHandler.getClientOrder().getOrdStatus()))
            {
//                LogFactory.warn(Alert.Severity.Minor,
//                        String.format(Alert.PAUSE_RESUME_ERROR, orderHandler.getClientOrder().getClientOrderId()),
//                        "Can't pause/resume a completed order!", null);
                LogFactory.debug("Can't pause/resume a completed order!\n"+String.format(Alert.PAUSE_RESUME_ERROR, orderHandler.getClientOrder().getClientOrderId()));
                return;
            }

            //Alert.clearAlert(String.format(Alert.PAUSE_RESUME_ERROR, orderHandler.getClientOrder().getClientOrderId()));
            PauseResumeEvaluationData pauseResumeEvaluationData = (PauseResumeEvaluationData)eventSource.getTriggerData();
            //LogFactory.info("trigger pause resume for order "+orderHandler.getClientOrder().getClientOrderId());

            if(pauseResumeEvaluationData==null)
                throw new IllegalArgumentException("No pauseResumeEvaluationData provided for "+orderHandler.getClientOrder().getClientOrderId());

            if(pauseResumeEvaluationData.getTradeAction()==TradeAction.Pause){
                orderHandler.pause(pauseResumeEvaluationData.isForceToApply());
            }else{
                orderHandler.resume(pauseResumeEvaluationData.isForceToApply());
            }

        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,
                    String.format(Alert.PAUSE_RESUME_ERROR, orderHandler.getAlertID()),
                    "Pause/Resume error!", ex);
        }
    }

}