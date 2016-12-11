package com.csc108.model.fixModel.order;

import com.csc108.model.ITimeable;

import java.time.LocalDateTime;

/**
 * Created by zhangbaoshan on 2016/9/6.
 */
public class OrderSnapshot implements ITimeable {
    private final LocalDateTime dateTime;
    private final String clientOrderId;
    private final long cumQty;
    private final double price;

    @Override
    public LocalDateTime getDateTime() {
        return dateTime ;
    }

    @Override
    public long getQty() {
        return cumQty;
    }

    public OrderSnapshot(String clientOrderId_,long qty_,double price_){
        dateTime = LocalDateTime.now();
        clientOrderId = clientOrderId_;
        cumQty = qty_;
        price = price_;
    }

    public static OrderSnapshot click(ClientOrder order){
        return new OrderSnapshot(order.getClientOrderId(),order.getCumQty(),order.getLastPrice());
    }
}
