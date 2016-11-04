package com.csc108.tradingRule.evaluators;

import com.csc108.tradingRule.core.BaseEvaluator;

/**
 * Created by zhangbaoshan on 2016/10/31.
 */
public class AccountIDEvaluator extends BaseEvaluator {
    @Override
    public String getEvaluatorName() {
        return "AccountIDEvaluator";
    }

    @Override
    protected boolean evaluate(){
        if(orderHandler.getClientOrder().getAccountId().isEmpty() ||orderHandler.getClientOrder().getAccountId()==null
                || orderHandler.getClientOrder().getAccountId().equals(""))
            throw new IllegalArgumentException ("No account id set for evaluation!");

        return this.orderHandler.getClientOrder().getAccountId().equals(criteria);
    }
}
