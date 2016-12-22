package com.csc108.decision.configuration;

import com.csc108.decision.IDecisionConfig;
import org.jdom2.Element;

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

    @Override
    public void init(Element configNode) {

    }
}
