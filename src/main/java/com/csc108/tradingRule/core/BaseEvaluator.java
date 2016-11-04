package com.csc108.tradingRule.core;

import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public abstract class BaseEvaluator implements IEvaluator {
    protected OrderHandler orderHandler;
    protected String criteria;

    @Override
    public String getEvaluatorName() {
        return "BaseEvaluator";
    }

    @Override
    public boolean evaluate(OrderHandler _orderHandler, String _criteria) throws Exception {
        if(_criteria==null || _criteria.isEmpty()){
            throw new IllegalArgumentException("Criteria is not set!");
        }

        orderHandler = _orderHandler;
        criteria = _criteria;

        return evaluate();
    }

    protected boolean evaluate(){
        return false;
    }
}
