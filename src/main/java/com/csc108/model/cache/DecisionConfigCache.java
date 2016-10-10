package com.csc108.model.cache;

import com.csc108.decision.IDecisionConfig;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
public class DecisionConfigCache<T extends IDecisionConfig> extends AlgoCache<String, T> {

    public void put(String clientOrderId,T t){
        super.put(clientOrderId,t);
    }

    public T get(String clientOrderId){
        T result=null;
        result= super.get(clientOrderId);
        return result;
    }
}
