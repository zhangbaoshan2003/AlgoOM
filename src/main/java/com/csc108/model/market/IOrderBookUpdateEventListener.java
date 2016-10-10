package com.csc108.model.market;

/**
 * Created by zhangbaoshan on 2016/5/31.
 */
public interface IOrderBookUpdateEventListener {
    public void orderBookUpdated(OrderBookUpdatedEvent event);
}
