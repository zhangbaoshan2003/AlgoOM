package com.csc108.tradingRule.evaluators;

import com.csc108.model.fix.order.OrderPool;
import com.csc108.tradingRule.core.BaseEvaluator;
import quickfix.field.OrdStatus;

/**
 * Created by zhangbaoshan on 2016/11/2.
 */
public class AlgoOrderTypeEvaluator extends BaseEvaluator {
    @Override
    public String getEvaluatorName() {
        return "AlgoOrderTypeEvaluator";
    }

    @Override
    protected boolean evaluate(){
        if(criteria.equals("Pegging") && orderHandler.isPeggingOrder()){
            return true;
        }

        if(criteria.equals("Conditional") && orderHandler.isConditionalOrder()){
            return true;
        }

        if(criteria.equals("Normal") && orderHandler.isPeggingOrder()==false)
            return true;

        return false;
    }

    @Override
    public void validate(String _criteria){
        if(_criteria.matches("(Normal|Pegging|Conditional)")==false)
            throw new IllegalArgumentException("Invalid criteria set for AlgoOrderTypeEvaluator @ "+_criteria);
    }

}
