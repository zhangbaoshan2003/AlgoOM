package com.csc108.model.market;

import com.csc108.model.IEvaluationData;

/**
 * Created by zhangbaoshan on 2016/8/3.
 */
public class OrderBookEvaluationData implements IEvaluationData {
    private final OrderBook orderBookUpdated;
    public OrderBookEvaluationData(OrderBook orderBook){
        this.orderBookUpdated = orderBook;
    }

    public OrderBook getOrderBookUpdated() {
        return orderBookUpdated;
    }
}
