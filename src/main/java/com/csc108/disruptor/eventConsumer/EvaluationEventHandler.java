package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.market.OrderBookEvaluationData;
import com.csc108.model.market.RealTimeMarketData;
import com.csc108.utility.Alert;
import com.csc108.utility.FixUtil;

/**
 * Created by LEGEN on 2016/5/14.
 */
public class EvaluationEventHandler extends EventHandlerBase {
    private long triggeredTime;

    public static EvaluationEventHandler  Instance= new EvaluationEventHandler    ();

    private EvaluationEventHandler (){
    }

    @Override
    public String getHandlerName(){
        return "EvaluationEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){
        if(eventSource==null)
            return;

        String clientOrderId="";
        OrderHandler clientOrderHandler = (OrderHandler)eventSource.getDataHandler();
        ClientOrder clientOrder = clientOrderHandler.getClientOrder();
        clientOrderId = clientOrder.getClientOrderId();

        if(eventSource.getDataHandler().getOriginalThreadId()!= Thread.currentThread().getId()){
            LogFactory.error("Evaluation event dispatch thread error", null);
        }

        if(FixUtil.IsOrderCompleted(clientOrder.getOrdStatus())){
            //System.out.println(String.format("Client order %s has been completed", clientOrder));
            return;
        }

        try{
            boolean flushLog=false;
            if(eventSource.getTriggerData() != null){
                if (eventSource.getTriggerData() instanceof OrderBookEvaluationData) {
                    flushLog=true;
                }
            }
            clientOrderHandler.process(flushLog);
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.PROCESS_ORDER_ERROR,clientOrderId), ex.getMessage(),ex);
        }
    }

    public long getTriggeredTime() {
        return triggeredTime;
    }

    public void setTriggeredTime(long triggeredTime) {
        this.triggeredTime = triggeredTime;
    }

}
