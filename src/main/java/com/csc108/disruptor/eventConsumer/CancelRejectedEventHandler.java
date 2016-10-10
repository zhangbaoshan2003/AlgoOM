package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.fix.FixEvaluationData;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.ExchangeOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.fix.order.OrderPool;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.field.ClOrdID;
import quickfix.field.OrdStatus;
import quickfix.field.OrigClOrdID;
import quickfix.fix42.OrderCancelReject;

/**
 * Created by zhangbaoshan on 2016/5/23.
 */
public class CancelRejectedEventHandler  extends EventHandlerBase  {
    public static CancelRejectedEventHandler   Instance= new CancelRejectedEventHandler  ();
    private CancelRejectedEventHandler(){
    }

    @Override
    public String getHandlerName(){
        return "CancelRejectedEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){
        OrderHandler clientOrderManager = (OrderHandler)eventSource.getDataHandler();
        ClientOrder clientOrder = clientOrderManager.getClientOrder();

        try{
            if(eventSource.getDataHandler().getOriginalThreadId()!= Thread.currentThread().getId()){
                LogFactory.error("CancelRejectedEventHandler Dispatch thread error", null);
            }

            FixEvaluationData fixEvaluationData = (FixEvaluationData)eventSource.getTriggerData();
            if(!(fixEvaluationData.getFixMsg() instanceof OrderCancelReject)){
                throw new IllegalArgumentException("Can't handel none cancel request message:"+fixEvaluationData.getFixMsg());
            }

            OrderCancelReject reject = (OrderCancelReject)fixEvaluationData.getFixMsg();
            ExchangeOrder exchangeOrder=null;

            OrigClOrdID origClOrdID = new OrigClOrdID();
            ClOrdID clOrdID = new ClOrdID();
            if(reject.isSet(origClOrdID)){
                reject.get(origClOrdID);
                exchangeOrder= OrderPool.getExchangeOrderMap().get(origClOrdID.getValue());
            }else{
                reject.get(clOrdID);
                exchangeOrder= OrderPool.getExchangeOrderMap().get(clOrdID.getValue());
            }
            if(exchangeOrder==null)
                throw new IllegalArgumentException("Can't find exchange order to process cancel reject msg : "+reject);

            if(FixUtil.IsOrderCompleted(exchangeOrder.getOrdStatus())==true){
                LogFactory.warn("Can't process reject cancel message for a completed order, whose current order status is +"+exchangeOrder.getFixStatusDisplay());
            }

            //Todo: potential issue when multiple exchange orders generated for a client order
            if(clientOrder.getOrderHandler()!=null)
                if(clientOrder.getOrderHandler().isPeggingOrder()==false){
                    //normal algo order, sent response back
                    if(clientOrder.getCancelRequestMsg()!=null){
                        //add check condition to avoid a cancel reject response from exchange directly, where no cancel request sent
                        clientOrder.restoreOrderStatusBeforeCancel();
                        FixMsgHelper.rejectCancelRequestToClient(clientOrder,clientOrder.getCancelRequestMsg(),"Cancel rejected from exchange!");
                    }

                }else{
                    DisruptorController controller = clientOrder.getOrderHandler().getController();
                    if(controller!=null){
                        controller.enqueueEvent(com.csc108.disruptor.event.EventType.EVALUATION, clientOrder.getOrderHandler(), null);
                    }
                }

        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.PROCESS_CANCEL_REJECT_ERROR,clientOrder.getClientOrderId()),ex.getMessage(),ex);
        }
    }
}
