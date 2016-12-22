package com.csc108.decision.configuration;

import com.csc108.decision.IDecisionConfig;
import com.csc108.decision.configuration.base.DecisionConfigBase;
import com.csc108.model.fixModel.order.OrderHandler;
import org.jdom2.Element;

/**
 * Created by zhangbaoshan on 2016/12/21.
 */
public class FinisherDecisionConfig extends DecisionConfigBase {
    private boolean enabled=false;
    private int finisherTimeOffInSeconds=60;

    @Override
    public void readConfigData(Element dataElement) throws Exception {
        enabled  = dataElement.getAttribute("Enable").getBooleanValue();
        finisherTimeOffInSeconds=dataElement.getAttribute("FinishTimeOffSetInSec").getIntValue();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getFinisherTimeOffInSeconds() {
        return finisherTimeOffInSeconds;
    }
}
