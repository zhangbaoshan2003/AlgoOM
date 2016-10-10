package com.csc108.decision.configuration;

import com.csc108.decision.IDecisionConfig;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
public class SplitOrderDecisionConfig implements IDecisionConfig {

    private final int numOfChildOrdersToGenerate;

    public SplitOrderDecisionConfig(int numOfChildOrdersToGenerate){
        this.numOfChildOrdersToGenerate = numOfChildOrdersToGenerate;
    }

    public int getNumOfChildOrdersToGenerate(){
        return this.numOfChildOrdersToGenerate;
    }
}
