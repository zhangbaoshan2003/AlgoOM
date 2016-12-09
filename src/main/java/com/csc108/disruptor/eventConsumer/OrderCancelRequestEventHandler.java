package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.event.OmEvent;
import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.drools.OrderMessage;
import com.csc108.log.LogFactory;
import com.csc108.model.OrderState;
import com.csc108.model.fix.FixEvaluationData;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.field.*;
import quickfix.fix42.OrderCancelRequest;

/**
 * Created by zhangbaoshan on 2016/5/6.
 * handle cancel order request from client
 */
public class OrderCancelRequestEventHandler extends EventHandlerBase {

    public static OrderCancelRequestEventHandler Instance= new OrderCancelRequestEventHandler();

    private OrderCancelRequestEventHandler(){

    }

    @Override
    public String getHandlerName(){
        return "OrderCancelRequestEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){
        if(eventSource.getDataHandler().getOriginalThreadId()!= Thread.currentThread().getId()){
            LogFactory.error("OrderCancelRequestEventHandler Dispatch thread error",null);
            //return;
        }
        logger.info(String.format("OrderCancelRequestHandler handle event %s @ thread %d",eventSource.getDataHandler().getID(),Thread.currentThread().getId()));

        OrderHandler clientOrderManager =  (OrderHandler)eventSource.getDataHandler();
        ClientOrder clientOrder = clientOrderManager.getClientOrder();
        FixEvaluationData triggerData= (FixEvaluationData)eventSource.getTriggerData();

        OrderCancelRequest cancelRequest=null;
        if(!(triggerData.getFixMsg() instanceof OrderCancelRequest)){
            throw new IllegalArgumentException("Can't handel cancel request without msg :"+triggerData.getFixMsg());
        }else{
            cancelRequest = (OrderCancelRequest)triggerData.getFixMsg();
        }

        //has been request cancel before or has been completed,reject the incoming cancel request

        if(clientOrder.getOrdStatus().getValue()== OrdStatus.PENDING_CANCEL){

            try{
                FixMsgHelper.rejectCancelRequestToClient(clientOrder, cancelRequest,
                        "Order has been requested to cancel before");
            }catch (Exception ex){
                Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.REJECT_CANCEL_ERROR_KEY,clientOrder.getClientOrderId()),
                        cancelRequest.toString(),ex);
            }finally {
                return;
            }
        }

        if(FixUtil.IsOrderCompleted(clientOrder.getOrdStatus())==true){
            try{
                FixMsgHelper.rejectCancelRequestToClient(clientOrder, cancelRequest,
                        "Can't cancel a completed order whose status is "+clientOrder.getOrdStatus());
            }catch (Exception ex){
                Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.REJECT_CANCEL_ERROR_KEY,clientOrder.getClientOrderId()),
                        cancelRequest.toString(),ex);
            }finally {
                return;
            }
        }
        //if there is no exchanger order sent out, if yes, cancel the client order directly
        if(clientOrderManager.getExchangeOrders().size()==0 ||
                clientOrderManager.getExchangeOrders().stream().
                        filter(x -> x.getOrderState() == OrderState.SENT_TO_EXCHANGE).findAny().isPresent()==false){

            try{
                clientOrderManager.getExchangeOrders().forEach(x -> x.setOrdStatus(new OrdStatus(OrdStatus.CANCELED)));

                clientOrder.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));

                FixMsgHelper.responseCancelRequestClientOrder(clientOrder, clientOrder.getOrdStatus(),
                        new ExecType(ExecType.CANCELED), FixMsgHelper.CLIENT_IN_REPORT_CANCELED, cancelRequest);

                clientOrderManager.publishMsg(false);


            }catch (Exception ex){
                Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.DIRECT_CANCEL_ORDER_ERROR_KEY,clientOrder.getClientOrderId()),
                        triggerData.getFixMsg().toString(),ex);
            }finally {
                return;
            }
        }

        //or else send cancel request to each
        try{
            clientOrder.setOrdStatus(new OrdStatus(OrdStatus.PENDING_CANCEL));
            clientOrderManager.publishMsg(false);
            FixMsgHelper.responseCancelRequestClientOrder(clientOrder, clientOrder.getOrdStatus(),
                    new ExecType(ExecType.PENDING_CANCEL), FixMsgHelper.CLIENT_PENDING_CANCEL_LOG, cancelRequest);

            //send out cancel request to all of exchange order sent out
            clientOrderManager.getExchangeOrders().stream()
                    .filter(x->x.getOrderState()==OrderState.SENT_TO_EXCHANGE && FixUtil.IsOrderCompleted(x.getOrdStatus())==false)
                    .forEach(x -> {
                        try {
                            x.setOrdStatus(new OrdStatus(OrdStatus.PENDING_CANCEL));
                            FixMsgHelper.cancelExchangeOrder(x);
                        } catch (Exception ex) {
                            Alert.fireAlert(Alert.Severity.Critical,
                                    String.format(Alert.CANCEL_EXG_ORDER_ERROR_KEY, x.getClientOrderId()),
                                    "Cancel exchange order by client failed!", ex);
                        }
                    });
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,
                    String.format(Alert.PROCESS_ORDER_ERROR,clientOrder.getClientOrderId()),
                    cancelRequest.toString(),ex);
        }
    }
}
