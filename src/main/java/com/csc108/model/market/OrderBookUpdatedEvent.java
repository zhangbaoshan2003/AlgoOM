package com.csc108.model.market;

import java.util.EventObject;

/**
 * Created by zhangbaoshan on 2016/5/31.
 */
public class OrderBookUpdatedEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public OrderBookUpdatedEvent(Object source) {
        super(source);
    }
}
