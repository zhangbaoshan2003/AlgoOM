package com.csc108.tradingRule.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/10/31.
 */
public class TradingRule implements IRule {
    private final String ruleName;
    private final ArrayList<HashMap<String,String>> evaluatorCriterias = new ArrayList<>();
    private final ArrayList<HashMap<String, LinkedHashMap<String, String>>> handlerParameters = new ArrayList<>();

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public List<HashMap<String, String>> getEvaluatorCriterias() {
        return evaluatorCriterias;
    }

    @Override
    public List<HashMap<String, LinkedHashMap<String, String>>> getHandlerParameters() {
        return handlerParameters;
    }

    public TradingRule(String _ruleName){
        ruleName = _ruleName;
    }
}
