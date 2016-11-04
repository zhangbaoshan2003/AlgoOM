package com.csc108.tradingRule.evaluators;

import com.csc108.model.fix.order.OrderHandler;
import com.csc108.tradingRule.core.IEvaluator;

/**
 * Created by zhangbaoshan on 2016/11/2.
 */
public class AlwaysTrueEvaluator implements IEvaluator {

    @Override
    public String getEvaluatorName() {
        return "AlwaysTrueEvaluator";
    }

    @Override
    public boolean evaluate(OrderHandler orderHandler, String criteria) throws Exception {
        return true;
    }
}
