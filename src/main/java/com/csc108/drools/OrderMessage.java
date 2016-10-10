package com.csc108.drools;

import quickfix.Message;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReject;
import quickfix.fix42.OrderCancelRequest;

import java.io.Serializable;

/**
 * Created by zhangbaoshan on 2016/5/23.
 */
public class OrderMessage implements Serializable {
    private static final long serialVersionUID = -8759352743389620367L;

    private final Message message;
    public OrderMessage(Message msg){
        this.message=msg;
    }

    public Message getMessage() {
        return message;
    }

    public boolean IsNewOrderMessage(){
        return this.message instanceof NewOrderSingle;
    }

    public boolean IsExecutionReport(){
        return this.message instanceof ExecutionReport;
    }

    public boolean IsCancelRejected(){
        return this.message instanceof OrderCancelReject;
    }

    public boolean IsCancelOrderMessage(){
        return this.message instanceof OrderCancelRequest;
    }
}
