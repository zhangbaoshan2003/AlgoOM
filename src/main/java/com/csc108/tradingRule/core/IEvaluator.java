package com.csc108.tradingRule.core;

import com.csc108.model.fixModel.order.OrderHandler;

/**
 * Created by LEGEN on 2016/10/29.
 */
public interface IEvaluator {
    String getEvaluatorName();
    boolean evaluate(OrderHandler orderHandler,String criteria) throws Exception;
    void validate(String criteria) throws IllegalArgumentException;
}
