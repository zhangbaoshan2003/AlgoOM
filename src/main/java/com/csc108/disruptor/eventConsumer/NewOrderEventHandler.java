package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.drools.OrderMessage;
import com.csc108.log.LogFactory;
import com.csc108.model.OrderState;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.RuleEngine;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.SystemTime;
import quickfix.field.OrdStatus;

/**
 * Created by zhangbaoshan on 2016/5/6.
 * handle new order request from client
 */
public class NewOrderEventHandler extends EventHandlerBase {

    public static NewOrderEventHandler Instance= new NewOrderEventHandler();

    private NewOrderEventHandler(){

    }

    @Override
    public String getHandlerName(){
        return "NewOrderEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){
        try{
            eventSource.getDataHandler().setOriginalThread(Thread.currentThread().getId());
            eventSource.getDataHandler().setHandledTimeStamp(SystemTime.currentTimeMillis());

            OrderHandler clientOrderHandler=  (OrderHandler)eventSource.getDataHandler();
            if(FixUtil.IsOrderCompleted(clientOrderHandler.getClientOrder().getOrdStatus()))
            {
                Alert.fireAlert(Alert.Severity.Minor,
                        String.format(Alert.NEW_HANDLE_ERROR,clientOrderHandler.getAlertID()),"Can't initialize client order more than once!",null);
                return;
            }

            OrderMessage orderMessage = new OrderMessage(clientOrderHandler.getClientOrder().getNewOrderRequestMsg());
            DroolsUtility.processMessage(orderMessage, DroolsType.NEW_SINGLE_ORDER_REQUEST);

            if(clientOrderHandler.getClientOrder().getOrderState()!=OrderState.INITIALIZED){
                clientOrderHandler.initialize();
                clientOrderHandler.getClientOrder().setOrderState(OrderState.INITIALIZED);

                //response new ack to client side
                FixMsgHelper.responseNewAckReport(clientOrderHandler.getClientOrder().getNewOrderRequestMsg(),
                        clientOrderHandler.getClientOrder().getSessionID());
                clientOrderHandler.getClientOrder().setOrdStatus(new OrdStatus(OrdStatus.NEW));

            }else{
                Alert.fireAlert(Alert.Severity.Minor,
                        String.format(Alert.INITIALIZE_CLIENT_ORDER_KEY,clientOrderHandler.getAlertID()),"Can't initialize client order more than once!",null);
            }

            RuleEngine.process(clientOrderHandler);

        }catch (Exception ex){
            LogFactory.error("handle new order event error!",ex);
        }
    }
}
