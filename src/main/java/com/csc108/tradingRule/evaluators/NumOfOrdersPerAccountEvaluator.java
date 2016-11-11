package com.csc108.tradingRule.evaluators;

import com.csc108.model.fix.order.OrderPool;
import com.csc108.tradingRule.core.BaseEvaluator;
import quickfix.field.OrdStatus;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class NumOfOrdersPerAccountEvaluator extends BaseEvaluator {
    @Override
    public String getEvaluatorName() {
        return "NumOfOrdersPerAccountEvaluator";
    }

    @Override
    protected boolean evaluate(){
        if(criteria.matches("(\\d+|Acct_\\w+):\\d+")==false)
            throw new IllegalArgumentException("Invalid criteria set for NumOfOrdersPerAccountEvaluator @ "+criteria);

        String[] paras = criteria.split(":");
        if(paras.length!=2)
            throw new IllegalArgumentException("Invalid criteria set for NumOfOrdersPerAccountEvaluator @ "+criteria);

        String accountId = paras[0];
        int numOfOrdersAllowed = Integer.parseInt(paras[1]);

        long totalOrders = OrderPool.getClientOrderMap().values().stream().filter(x -> x.getAccountId().equals(accountId) &&
                 x.getOrdStatus().getValue() != OrdStatus.REJECTED)
                 .count();

        return numOfOrdersAllowed<totalOrders;
    }
}
