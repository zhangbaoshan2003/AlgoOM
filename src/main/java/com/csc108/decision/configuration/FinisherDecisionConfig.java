package com.csc108.decision.configuration;

import com.csc108.decision.IDecisionConfig;
import org.jdom2.Element;

/**
 * Created by zhangbaoshan on 2016/12/21.
 */
public class FinisherDecisionConfig implements IDecisionConfig {

    private boolean enabled=false;
    private int finisherTimeOffInSeconds=60;

    @Override
    public void init(Element configNode) throws Exception {
        enabled = configNode.getAttribute("Enable").getBooleanValue();
        finisherTimeOffInSeconds = configNode.getAttribute("FinishTimeOffSetInSec").getIntValue();
    }
}
