package com.csc108.decision.configuration;

import com.csc108.decision.IDecisionConfig;

/**
 * Created by LEGEN on 2016/10/29.
 */
public class AccountMaximumOrdersConfig implements IDecisionConfig {
    int maximumOrdersAllowed=-1;

    public AccountMaximumOrdersConfig(){

    }

    public void setMaximumOrdersAllowed(int ordersNumAllowed){
        this.maximumOrdersAllowed = ordersNumAllowed;
    }

    public int getMaximumOrdersAllowed(){
        return maximumOrdersAllowed;
    }
}
