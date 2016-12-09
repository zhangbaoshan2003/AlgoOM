package com.csc108.tradingRule.handlers;

import com.csc108.decision.BaseDecision;
import com.csc108.decision.DecisionChainManager;
import com.csc108.decision.algo.DeliverToEngineDecision;
import com.csc108.decision.algo.PauseResumeDecision;
import com.csc108.decision.pegging.PeggingDecision;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.fix.sessionPool.AlgoSessionPoolPicker;
import com.csc108.model.fix.sessionPool.PeggingSessionPoolPicker;
import com.csc108.tradingRule.core.IHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by zhangbaoshan on 2016/11/24.
 */
public class SessionPickerAssignHandler implements IHandler {

    @Override
    public String getHandlerName() {
        return "SessionPickerAssignHandler";
    }

    @Override
    public void handle(OrderHandler orderHandler, LinkedHashMap<String, String> parameters) {

        if(parameters.keySet().contains("PickerName")==false)
            throw new IllegalArgumentException("PickerName paramter is expected!");

        String pickerName = parameters.get("PickerName");

        if(pickerName.equalsIgnoreCase("PeggingSessionPoolPicker")==true){
            orderHandler.setSessionPoolPicker(new PeggingSessionPoolPicker());
        }else if(pickerName.equalsIgnoreCase("AlgoSessionPoolPicker")==true){
            orderHandler.setSessionPoolPicker(new AlgoSessionPoolPicker());
        }else{
            throw new IllegalArgumentException(String.format( "Session pciker name %s is invalid",pickerName));
        }
    }
}
