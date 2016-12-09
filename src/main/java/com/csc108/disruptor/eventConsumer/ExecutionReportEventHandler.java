package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.*;
import com.csc108.disruptor.event.EventType;
import com.csc108.log.LogFactory;
import com.csc108.model.OrderState;
import com.csc108.model.fix.FixEvaluationData;
import com.csc108.model.fix.order.*;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

/**
 * Created by zhangbaoshan on 2016/5/8.
 */
public class ExecutionReportEventHandler extends EventHandlerBase {
    public static ExecutionReportEventHandler Instance= new ExecutionReportEventHandler();
    private ExecutionReportEventHandler(){

    }

    @Override
    public String getHandlerName(){
        return "ExecutionReportEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){
        if(eventSource.getDataHandler().getOriginalThreadId()!= Thread.currentThread().getId()){
            LogFactory.error("ExecutionReportEventHandler Dispatch thread error", null);
            //return;
        }

        //logger.info(String.format("ExecutionReportEventHandler handle event %s @ thread %d",eventSource.getDataHandler().getID(),Thread.currentThread().getId()));

        OrderHandler orderHandler= (OrderHandler)eventSource.getDataHandler();
        ClientOrder clientOrder = orderHandler.getClientOrder();

        if(orderHandler==null)
            throw new IllegalArgumentException("Exchange order is nu11!");

        try{

            FixEvaluationData fixEvaluationData = (FixEvaluationData)eventSource.getTriggerData();
            ExecutionReport executionReport= (ExecutionReport)fixEvaluationData.getFixMsg();

            //locate the exchange order related to
            String orderID="";
            ExchangeOrder exchangeOrder=null;
            OrigClOrdID origClOrdID = new OrigClOrdID();
            ClOrdID clOrdID = new ClOrdID();

            if(executionReport.isSet(origClOrdID)){
                executionReport.get(origClOrdID);
                orderID=origClOrdID.getValue();
                exchangeOrder= OrderPool.getExchangeOrderMap().get(origClOrdID.getValue());
            }else{
                executionReport.get(clOrdID);
                orderID = clOrdID.getValue();
                exchangeOrder= OrderPool.getExchangeOrderMap().get(clOrdID.getValue());
            }

            if(exchangeOrder==null){
                throw new IllegalArgumentException("Can't find exchange order to process "+executionReport);
            }

            ////update exchange order status
            //processExchangeOrder(executionReport, exchangeOrder);
            exchangeOrder.processExecutionReport(executionReport);

            ////based on latest exchange order status, response to client
            //orderHandler.processClientOrderExecutionReport(executionReport);
            clientOrder.processExecutionReport(executionReport);

            orderHandler.publishMsg(false);




        }catch (Exception ex){
            LogFactory.error("Execution report handler process error!",ex);
        }
    }
}
