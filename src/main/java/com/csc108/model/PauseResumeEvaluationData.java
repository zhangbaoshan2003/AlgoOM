package com.csc108.model;

import com.csc108.model.criteria.TradeAction;

/**
 * Created by LEGEN on 2016/6/5.
 */
public class PauseResumeEvaluationData implements IEvaluationData {
    private final TradeAction tradeAction;
    private final boolean forceToApply;

    public PauseResumeEvaluationData (TradeAction tradeAction,boolean forceToApply){
        this.tradeAction= tradeAction;
        this.forceToApply=forceToApply;
    }

    public TradeAction getTradeAction() {
        return tradeAction;
    }

    public boolean isForceToApply() {
        return forceToApply;
    }
}
