package com.csc108.tradingRule.handlers;

import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.utility.Alert;

import java.util.LinkedHashMap;

/**
 * Created by zhangbaoshan on 2016/11/8.
 */
public class CallingOrderHandlerProcessHandler implements IHandler {


    @Override
    public String getHandlerName() {
        return "CallingOrderHandlerProcessHandler";
    }

    @Override
    public void handle(OrderHandler orderHandler, LinkedHashMap<String, String> parameters) {
        try{
            //put the order to process in the orderpool
//            if(OrderPool.getClientOrderMap().putIfAbsent(orderHandler.getClientOrder().getClientOrderId(), orderHandler.getClientOrder())!=null){
//                LogFactory.debug(String.format("Order %s already existed in order client pool!", orderHandler.getClientOrder().getClientOrderId()));
//            }

            boolean flushLog=false;
            if(parameters.keySet().contains("flushLog")){
                flushLog = Boolean.parseBoolean(parameters.get("flushLog")) ;
            }

            //set flag to indicate if report progress is needed
            orderHandler.setReportProgressNeeded(flushLog);

            orderHandler.process();

        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major, String.format(Alert.PROCESS_ORDER_ERROR, orderHandler.getAlertID()), ex.getMessage(), ex);
        }
    }
}
