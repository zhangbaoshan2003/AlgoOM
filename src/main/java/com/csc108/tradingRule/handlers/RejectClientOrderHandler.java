package com.csc108.tradingRule.handlers;

import com.csc108.model.data.OrderSide;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.field.OrdStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class RejectClientOrderHandler implements IHandler {

    @Override
    public String getHandlerName() {
        return "RejectClientOrderHandler";
    }

    @Override
    public void handle(OrderHandler handler, LinkedHashMap<String, String> parameters) {
        if(handler.getClientOrder().getNewOrderRequestMsg()==null)
            throw new IllegalArgumentException("No request fix msg found @ "+handler.getClientOrder().getClientOrderId());

        ClientOrder clientOrder = handler.getClientOrder();

        if(FixUtil.IsOrderCompleted(clientOrder.getOrdStatus())==true){
            throw new IllegalArgumentException(String.format("Can't reject a %s order @ %s",clientOrder.getFixStatusDisplay(),clientOrder.getClientOrderId()));
        }

        if(clientOrder.getOrdStatus().getValue()== OrdStatus.PENDING_CANCEL){
            throw new IllegalArgumentException(String.format("Can't reject a %s order @ %s",clientOrder.getFixStatusDisplay(),clientOrder.getClientOrderId()));
        }

        FixMsgHelper.rejectClientOrder(clientOrder);
    }
}
