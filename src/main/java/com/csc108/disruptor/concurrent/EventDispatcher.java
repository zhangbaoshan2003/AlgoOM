package com.csc108.disruptor.concurrent;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.exceptions.InvalidEventException;
import com.csc108.log.LogFactory;
import com.csc108.model.IDataHandler;
import com.csc108.model.IEvaluationData;
import com.csc108.model.PauseResumeEvaluationData;
import com.csc108.model.WakeupEvaluationData;
import com.csc108.model.criteria.TradeAction;
import com.csc108.model.fix.FixEvaluationData;
import com.csc108.model.fix.order.*;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.SessionID;
import quickfix.SystemTime;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrigClOrdID;
import quickfix.fix42.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
public class EventDispatcher implements ILifetimeCycle {

    private static EventDispatcher instance=new EventDispatcher();

//    //hash index on which event should go to which disruptor controller
//    private final ConcurrentHashMap<String,DisruptorController> controllerMap = new ConcurrentHashMap<>();
//    public ConcurrentHashMap<String,DisruptorController> getControllerMap(){
//        return controllerMap;
//    }

    //hold pointer to all of available disruptor controller
    private final ConcurrentLinkedQueue<DisruptorController> disruptorPool = new ConcurrentLinkedQueue<DisruptorController>();

    //hash index for which controller should response orderbook updated event
    private final ConcurrentHashMap<String,DisruptorController> securityControllerMap = new ConcurrentHashMap<>();

    private EventDispatcher(){

    }

    public static EventDispatcher getInstance() {
        return instance;
    }

    @Override
    public void start() {
        LogFactory.info("Initializing event dispatcher pool ...");
        int threadNum=0;
        while (threadNum< GlobalConfig.getThreadNums()){
            try{
                DisruptorController disruptorController = new DisruptorController(threadNum);
                disruptorController.start();
                disruptorPool.add(disruptorController);
                threadNum++;
            }catch (Exception ex){
                LogFactory.error("start disruptor error!",ex);
            }
        }
    }

    @Override
    public void stop() {

        while (disruptorPool.size()>0){
            try{
                disruptorPool.poll().stop();
            }catch (Exception ex){
                LogFactory.error("stop disruptor error!",ex);
            }
        }
    }

    public void dispatchEvent(EventType eventType, IEvaluationData dataTrigger){

        if(eventType==EventType.NEW_SINGLE_ORDER ){
            //pick up a controller and setting the link in a robbin ring way
            try{
                FixEvaluationData fixEvaluationData = (FixEvaluationData)dataTrigger;
                NewOrderSingle newOrderSingle = (NewOrderSingle)fixEvaluationData.getFixMsg().clone();

                SessionID sessionID = fixEvaluationData.getSessionID();
                ClientOrder clientOrder = new ClientOrder(newOrderSingle,sessionID);

                DisruptorController controller =null;

                //should use lock , for performance reason, didn't
                synchronized (disruptorPool){
                    LogFactory.debug("handle new order event for "+clientOrder.getClientOrderId() );
                    controller = disruptorPool.poll();
                    if(controller==null){
                        LogFactory.debug("can't find valid controller for "+clientOrder.getClientOrderId());
                        LogFactory.error("can't find valid controller for "+clientOrder.getClientOrderId(),null);
                        throw new Exception("Null controller found!");
                    }
                    disruptorPool.add(controller);
                }

                if(OrderPool.getClientOrderMap().putIfAbsent(clientOrder.getClientOrderId(), clientOrder)!=null){
                    LogFactory.debug(String.format("Order %s already existed in order client pool!", clientOrder.getClientOrderId()));
                }

                IDataHandler handler = new OrderHandler(clientOrder,controller);
                handler.setCreatedTimeStamp(SystemTime.currentTimeMillis());
                controller.enqueueEvent(eventType, handler, dataTrigger);
                LogFactory.debug(String.format("Order %s has been put into ring buffer !", clientOrder.getClientOrderId()));

            }catch (Exception ex){
                LogFactory.error("Dispatch event error for event type "+eventType,ex);
            }
        }

        if(eventType==EventType.CANCEL_ORDER_REQUEST ){
            try{
                FixEvaluationData fixEvaluationData = (FixEvaluationData)dataTrigger;
                OrderCancelRequest cancelRequest = (OrderCancelRequest)fixEvaluationData.getFixMsg().clone();

                String origClOrdID = cancelRequest.getString(FixUtil.ORIG_CLIENT_ORDER_ID);
                ClientOrder clientOrder= OrderPool.getClientOrderMap().get(origClOrdID);

                if(clientOrder==null){
                    ManuallyOrder manuallyOrderToCancel  = OrderPool.getManuallyOrderMap().get(origClOrdID);
                    if(manuallyOrderToCancel!=null){
                        manuallyOrderToCancel.notifyClient(cancelRequest);
                        return;
                    }
                    throw new InvalidEventException("Can't find order to cancel for @"+cancelRequest);
                }

                LogFactory.omLog(FixMsgHelper.CLIENT_CANCEL_ORDER_LOG, clientOrder.getClientOrderId(), cancelRequest);

                clientOrder.setCancelRequestMsg(cancelRequest);

                DisruptorController controller = clientOrder.getOrderHandler().getController();

                if(controller==null){
                    throw new IllegalArgumentException("Can't find original client order for it's cancel request @"
                            +cancelRequest);
                }

                controller.enqueueEvent(eventType, clientOrder.getOrderHandler(),dataTrigger);

            }catch (Exception ex){
                LogFactory.error("Dispatch event error for event type "+eventType,ex);
            }
        }

        if(eventType==EventType.PAUSE_RESUME ){
            try{
                FixEvaluationData fixEvaluationData = (FixEvaluationData)dataTrigger;
                OrderPauseResumeRequest pauseResumeRequest= (OrderPauseResumeRequest)fixEvaluationData.getFixMsg().clone();

                String cloID = pauseResumeRequest.getString(FixUtil.CLIENT_ORDER_ID);
                ClientOrder clientOrder= OrderPool.getClientOrderMap().get(cloID);

                DisruptorController controller = clientOrder.getOrderHandler().getController();
                //controllerMap.get(clientOrder.getClientOrderId());
                if(controller==null){
                    throw new IllegalArgumentException("Can't find original client order for it's cancel request @"
                            +pauseResumeRequest);
                }

                PauseResumeEvaluationData evaluationData= new PauseResumeEvaluationData(TradeAction.Pause,false);
                controller.enqueueEvent(EventType.PAUSE_RESUME,clientOrder.getOrderHandler(),evaluationData);

            }catch (Exception ex){
                LogFactory.error("Dispatch event error for event type "+eventType,ex);
            }
        }

        if(eventType==EventType.EXECUTION_REPORT ){
            try{
                FixEvaluationData fixEvaluationData = (FixEvaluationData)dataTrigger;
                ExecutionReport executionReport= (ExecutionReport)fixEvaluationData.getFixMsg();
                SessionID sessionID = fixEvaluationData.getSessionID();

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
                    //check if this is a manually order
                    ManuallyOrder manuallyOrder = OrderPool.getManuallyOrderMap().get(orderID);

                    //process manually order new
                    if(executionReport.isSetField(ManuallyOrder.TAG_MANUA_MASTER_ORDER_ID)){
                        ExecType execType = (executionReport).getExecType();
                        switch (execType.getValue()){
                            case ExecType.NEW:
                                try{
                                    ManuallyOrder manuallyOrderNew = new ManuallyOrder(executionReport);
                                    OrderPool.getManuallyOrderMap().put(manuallyOrderNew.getManuallyClOrdID(), manuallyOrderNew);
                                    manuallyOrderNew.processReportAndNotifyClient(executionReport);
                                }catch (Exception ex){
                                    com.csc108.log.LogFactory.error("New manually order failed!",ex);
                                }
                                break;

                            case ExecType.CANCELED:
                                try{
                                    if(manuallyOrder!=null){
                                        manuallyOrder.processReportAndNotifyClient(executionReport);
                                    }else{
                                        LogFactory.error("Can't find a manually order to cancel for @"+executionReport,null);
                                    }
                                }catch (Exception ex){
                                    com.csc108.log.LogFactory.error("Cancel manually order failed!@"+executionReport,ex);
                                }
                                break;

                            default:
                                LogFactory.error("Unknown exec type for manually order@"+executionReport,null);
                        }
                        return;
                    }else if(manuallyOrder!=null){//process existed manually order
                        try{
                            manuallyOrder.processReportAndNotifyClient(executionReport);
                        }catch (Exception ex){
                            LogFactory.error("Process manually order error! @"+executionReport,null);
                        }
                        return;
                    }
                    throw new InvalidEventException("Can't find exchange order for report @"+executionReport);
                }

                OrderHandler orderHandler= exchangeOrder.getParent().getOrderHandler();
                DisruptorController controller = orderHandler.getController();

                // controllerMap.get(clientOrder);
                if(controller==null){
                    throw new IllegalArgumentException("Can't find original client order for it's exchange orders execution report@"+executionReport);
                }
                controller.enqueueEvent(eventType, orderHandler, dataTrigger);

                //schedule an evaluation event to process client order based on a execution report from exchange
                //controller.enqueueEvent(EventType.EXECUTION_REPORT,exchangeOrderManager.ge);

            }catch (Exception ex){
                LogFactory.error("Dispatch event error for event type "+eventType,ex);
            }
        }

        if(eventType==EventType.CANCEL_REJECTED ){
            try{
                FixEvaluationData fixEvaluationData = (FixEvaluationData)dataTrigger;
                OrderCancelReject rejectReport= (OrderCancelReject)fixEvaluationData.getFixMsg();

                ExchangeOrder exchangeOrder=null;
                OrigClOrdID origClOrdID = new OrigClOrdID();
                ClOrdID clOrdID = new ClOrdID();

                if(rejectReport.isSet(origClOrdID)){
                    rejectReport.get(origClOrdID);
                    exchangeOrder= OrderPool.getExchangeOrderMap().get(origClOrdID.getValue());
                }else{
                    rejectReport.get(clOrdID);
                    exchangeOrder= OrderPool.getExchangeOrderMap().get(clOrdID.getValue());
                }

                if(exchangeOrder==null){
                    //check if it's a cancel reject for a manually order
                    String manuallyOrderId= origClOrdID.getValue();
                    ManuallyOrder manuallyOrder = OrderPool.getManuallyOrderMap().get(manuallyOrderId);
                    if(manuallyOrder==null){
                        throw new InvalidEventException("Can't find exchange order nor manually order for order cancel reject @"+rejectReport);
                    }else{
                        manuallyOrder.notifyClient(rejectReport);
                        //can't process subsequent logic
                        return;
                    }
                }

                OrderHandler orderHandler= exchangeOrder.getParent().getOrderHandler();
                DisruptorController controller = orderHandler.getController();

                if(controller==null){
                    throw new IllegalArgumentException("Can't find original client order for it's cancel rejected response@"+rejectReport);
                }
                controller.enqueueEvent(eventType, orderHandler, dataTrigger);

            }catch (Exception ex){
                LogFactory.error("Dispatch event error for event type "+eventType,ex);
            }
        }
    }
}
