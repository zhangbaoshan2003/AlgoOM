package com.csc108.utility;

import com.csc108.disruptor.concurrent.EventDispatcher;
import com.csc108.disruptor.event.EventType;
import com.csc108.log.LogFactory;
import com.csc108.model.OrderState;
import com.csc108.model.fix.FixEvaluationData;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.ExchangeOrder;
import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.*;

import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
public class FixMsgHelper {

    public static final String CLIENT_PENDING_NEW_LOG="CLIENT << Exec Report (Pending New)";
    public static final String CLIENT_PENDING_CANCEL_LOG="CLIENT << Exec Report (Pending Cancel)";
    public static final String CLIENT_NEW_ACK_LOG="CLIENT << Exec Report (New)";
    public static final String CLIENT_NEW_ORDER_LOG="CLIENT >> New Single Order";
    public static final String CLIENT_CANCEL_ORDER_LOG="CLIENT >> Order Cancel Request";

    public static final String EXG_NEW_ORDER_LOG="EXG <- New Single Order";
    public static final String EXG_REPORT_LOG="EXG <- Exe Report";
    public static final String EXG_OUT_ACK="EXG -> New Ack";

    public static final String EXG_OUT_PARTIAL_FILL="EXG -> Exec Report(Partial Fill)";
    public static final String EXG_OUT_FILL="EXG -> Exec Report(Fill)";
    public static final String EXG_OUT_PENDING_CANCEL="EXG -> Exec Report(Pending Cancel)";
    public static final String EXG_OUT_CANCELED="EXG -> Exec Report(Canceled)";
    public static final String EXG_OUT_EXPIRED="EXG -> Exec Report(Expired)";
    public static final String EXG_OUT_REJECTED="EXG -> Exec Report(Rejected)";
    public static final String EXG_OUT_PENDING_NEW="EXG -> Exec Report(Pending New)";

    public static final String CLIENT_IN_PARTIAL_FILL="CLIENT << Exec Report(Partial Fill)";
    public static final String CLIENT_IN_FILL="CLIENT << Exec Report(Fill)";
    public static final String CLIENT_IN_REJECTED="CLIENT << Exec Report(Rejected)";
    public static final String CLIENT_IN_EXPIRED="CLIENT << Exec Report(Expired)";
    public static final String CLIENT_IN_CANCEL_REJECT="CLIENT << Cancel Rejected";
    public static final String CLIENT_IN_REPORT_CANCELED="CLIENT << Exec Report(Canceled)";
    public static final String EXG_OUT_CANCEL_ORDER="EXG <- Cancel Request";

    public static final String CLIENT_IN_REPORT_CANCELED_MANUALLY="CLIENT << Exec Report(Canceled by command)";

    public static final String EXG_OUT_PAUSE_ORDER="EXG <- Pause Request";
    public static final String EXG_OUT_RESUME_ORDER="EXG <- Resume Request";
    public static final String EXG_IN_RESUME_ORDER="EXG -> Restated";

    public static final String CLIENT_IN_PAUSE="CLIENT << Restated";

    public static void sendMessage(quickfix.Message message, SessionID sessionid,String logMessage,String tag)
            throws SessionNotFound, FieldNotFound {

        if(sessionid==null ||
                Session.lookupSession(sessionid)==null ||
                Session.lookupSession(sessionid).isLoggedOn()==false){
            throw new SessionNotFound(String.format("Msg %s sent out failed due to the session %s not logged on!",message.toString(),sessionid));
        }

        quickfix.fix42.Message.Header header = (quickfix.fix42.Message.Header) message.getHeader();
        header.setField(header.getBeginString());
        header.setField(new SenderCompID(sessionid.getSenderCompID()));
        header.setField(new TargetCompID(sessionid.getTargetCompID()));
        quickfix.fix42.Message fixMsg = (quickfix.fix42.Message) message;

        if(logMessage.isEmpty()==false){
            LogFactory.omLog(logMessage, tag, fixMsg);
        }
        quickfix.Session.sendToTarget(message);
    }

    private static void responsePendingNewReport(NewOrderSingle request, SessionID sessionID){
        String clientOrderId="";
        try {
            clientOrderId =request.getString(FixUtil.CLIENT_ORDER_ID);
            LogFactory.omLog(CLIENT_NEW_ORDER_LOG, clientOrderId, request);
            ExecutionReport acknowledgement = new ExecutionReport(
                    new OrderID(clientOrderId),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.PENDING_NEW),
                    new OrdStatus(OrdStatus.PENDING_NEW),
                    new Symbol(request.getSymbol().getValue()),
                    request.getSide(),
                    new LeavesQty(0),
                    new CumQty(request.getOrderQty().getValue()),
                    new AvgPx(0)
            );
            acknowledgement.set(new ClOrdID(clientOrderId));
            sendMessage(acknowledgement, sessionID, CLIENT_PENDING_NEW_LOG, request.getString(FixUtil.CLIENT_ORDER_ID));
            //LogFactory.omLog(CLIENT_PENDING_NEW_LOG,clientOrderId,acknowledgement);
        }catch (SessionNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.SESSION_NOT_FOUND_KEY,sessionID,clientOrderId),
                    request.toString(),ex);
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.FIELD_NOT_FOUND_KEY,"BeginString",clientOrderId),
                    request.toString(),ex);
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Fatal,String.format(Alert.SENDING_MSG_ERROR,clientOrderId),
                    ex.getMessage(),ex);
        }
    }

    public static void responseRestatementReport(ExecutionReport responseReport,ClientOrder clientOrder){
        try {
            //ExecutionReport executionReport = new ExecutionReport();
            ExecutionReport executionReport = new ExecutionReport(
                    new OrderID(clientOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.RESTATED),
                    new OrdStatus(OrdStatus.SUSPENDED),
                    new Symbol(clientOrder.getSymbol()),
                    clientOrder.getOrderSide(),
                    new LeavesQty(clientOrder.getLeavesQty()),
                    new CumQty(clientOrder.getCumQty()),
                    new AvgPx(clientOrder.getLastPrice())
            );
            String logMsg="";
            StrategyStatusType strategyStatusType = new StrategyStatusType();
            int type= responseReport.getInt(strategyStatusType.getField());
            if(type==1){
                logMsg=FixMsgHelper.CLIENT_IN_PAUSE+" [resumed]";
                clientOrder.setPauseResumeState(OrderState.RESUMED);
                executionReport.setString(11200, "1");

            }else if(type==2){
                logMsg=FixMsgHelper.CLIENT_IN_PAUSE+" [paused]";
                clientOrder.setPauseResumeState(OrderState.PAUSED);
                executionReport.setString(11200, "2");
            }else if(type==3){
                logMsg=FixMsgHelper.CLIENT_IN_PAUSE+" [pause with cancel]";
                clientOrder.setPauseResumeState(OrderState.PAUSED);
                executionReport.setString(11200,"3");
            }else{
               throw new IllegalArgumentException("Unrecognized restatement report!@"+responseReport);
            }
            responseReport.set(new ClOrdID(clientOrder.getClientOrderId()));
            FixMsgHelper.sendMessage(responseReport,clientOrder.getSessionID(),logMsg,clientOrder.getClientOrderId());

        }catch (SessionNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.SESSION_NOT_FOUND_KEY,clientOrder.getSessionID(),clientOrder.getClientOrderId()),
                    responseReport.toString(),ex);
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.FIELD_NOT_FOUND_KEY,"BeginString",clientOrder.getClientOrderId()),
                    responseReport.toString(),ex);
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Fatal,String.format(Alert.SENDING_MSG_ERROR,clientOrder.getClientOrderId()),
                    ex.getMessage(),ex);
        }
    }

    public static void responseNewAckReport(NewOrderSingle request, SessionID sessionID){
        String clientOrderId="";
        try {
            clientOrderId =request.getString(FixUtil.CLIENT_ORDER_ID);

            ExecutionReport acknowledgement = new ExecutionReport(
                    new OrderID(clientOrderId),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.NEW),
                    new OrdStatus(OrdStatus.NEW),
                    new Symbol(request.getSymbol().getValue()),
                    request.getSide(),
                    new LeavesQty(0),
                    new CumQty(request.getOrderQty().getValue()),
                    new AvgPx(0)
            );
            acknowledgement.set(new ClOrdID(clientOrderId));
            sendMessage(acknowledgement, sessionID,
                    FixMsgHelper.CLIENT_NEW_ACK_LOG, clientOrderId);
        }catch (SessionNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.SESSION_NOT_FOUND_KEY,sessionID,clientOrderId),
                    request.toString(),ex);
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.FIELD_NOT_FOUND_KEY,"BeginString",clientOrderId),
                    request.toString(),ex);
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Fatal,String.format(Alert.SENDING_MSG_ERROR,clientOrderId),
                    ex.getMessage(),ex);
        }
    }

    public static void rejectCancelRequestToClient(ClientOrder clientOrder,OrderCancelRequest cancelRequest,
                                            String rejectReason) throws Exception {
        OrderCancelReject cancelRejectedReport = new OrderCancelReject(new OrderID(clientOrder.getClientOrderId()),
                new ClOrdID(cancelRequest.getString(11)),
                new OrigClOrdID(clientOrder.getClientOrderId()),
                new OrdStatus(clientOrder.getOrdStatus().getValue()),
                new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST)
        );
        cancelRejectedReport.set(new Text(rejectReason));
        sendMessage(cancelRejectedReport, clientOrder.getSessionID(),
                FixMsgHelper.CLIENT_IN_CANCEL_REJECT, clientOrder.getClientOrderId());
    }

    public static void responseCancelRequestClientOrder(ClientOrder clientOrder,OrdStatus ordStatus,
                                                ExecType execType,String logMsg,
                                                OrderCancelRequest cancelRequest) throws Exception {
        clientOrder.setOrdStatus(ordStatus);
        ExecutionReport canceledReport = new ExecutionReport(
                new OrderID(clientOrder.getClientOrderId()),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                execType,
                ordStatus,
                new Symbol(clientOrder.getSymbol()),
                clientOrder.getOrderSide(),
                new LeavesQty(clientOrder.getLeavesQty()),
                new CumQty(clientOrder.getCumQty()),
                new AvgPx(clientOrder.getPrice())
        );
        canceledReport.set(new ClOrdID(cancelRequest.getString(11)));
        canceledReport.set(new OrigClOrdID(clientOrder.getClientOrderId()));
        canceledReport.set(new LastPx(clientOrder.getPrice()));
        canceledReport.set(new LastShares(clientOrder.getLeavesQty()));
        canceledReport.set(new OrderQty(clientOrder.getOrderQty()));
        sendMessage(canceledReport, clientOrder.getSessionID(), logMsg, clientOrder.getClientOrderId());
    }

    public static void handleExecutionReport(ExecutionReport report,SessionID sessionID)  {
        FixEvaluationData data = new FixEvaluationData(report,sessionID);
        EventDispatcher.getInstance().dispatchEvent(EventType.EXECUTION_REPORT, data);
    }

    public static void cancelExchangeOrder(ExchangeOrder exchangeOrder) throws Exception {

        OrderCancelRequest cancelRequest=null;

        if(exchangeOrder.getParent().getCancelRequestMsg()!=null){
            cancelRequest = (OrderCancelRequest )exchangeOrder.getParent().getCancelRequestMsg().clone();
        }else{
            cancelRequest =new OrderCancelRequest(new OrigClOrdID(exchangeOrder.getClientOrderId()),
                    new ClOrdID(UUID.randomUUID().toString()),new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),new TransactTime());
        }

        cancelRequest.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
        cancelRequest.set(new ClOrdID(UUID.randomUUID().toString()));

        //sometime engine send out cancel qty is not long, but float
        cancelRequest.set(new OrderQty(exchangeOrder.getLeavesQty()));

        exchangeOrder.setCancelRequestMsg(cancelRequest);

        sendMessage(cancelRequest, exchangeOrder.getSessionID(), EXG_OUT_CANCEL_ORDER, exchangeOrder.getParent().getClientOrderId());
    }

    public static void sendOutExchangeOrder(ExchangeOrder exchangeOrder) throws Exception {
        NewOrderSingle newOrderSingle = (NewOrderSingle)exchangeOrder.getParent().getNewOrderRequestMsg().clone();
        newOrderSingle.set(new ClOrdID(exchangeOrder.getClientOrderId()));
        newOrderSingle.set(new OrderQty(exchangeOrder.getOrderQty()));
        newOrderSingle.set(new Price(exchangeOrder.getPrice()));
        sendMessage(newOrderSingle, exchangeOrder.getSessionID(), EXG_NEW_ORDER_LOG, exchangeOrder.getParent().getClientOrderId());
        exchangeOrder.setOrderState(OrderState.SENT_TO_EXCHANGE);

//        try{
//
//
//        }catch (SessionNotFound ex){
//            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.SESSION_NOT_FOUND_KEY,exchangeOrder.getSessionID(),
//                            exchangeOrder.getClientOrderId()),
//                    newOrderSingle.toString(),ex);
//        }catch (FieldNotFound ex){
//            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.FIELD_NOT_FOUND_KEY,ex.field,exchangeOrder.getClientOrderId()),
//                    newOrderSingle.toString(),ex);
//        }catch (Exception ex){
//            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.SENDING_MSG_ERROR,exchangeOrder.getClientOrderId()),
//                    ex.getMessage(),ex);
//        }
    }

    /*incoming message handling*/
    public static void handleNewOrderRequest(NewOrderSingle newOrderSingle,SessionID sessionID)  {
        responsePendingNewReport(newOrderSingle, sessionID);
        FixEvaluationData data = new FixEvaluationData(newOrderSingle,sessionID);
        EventDispatcher.getInstance().dispatchEvent(EventType.NEW_SINGLE_ORDER,data);
    }

    public static void handleCancelOrderRequest(OrderCancelRequest cancelRequest,SessionID sessionID)  {
        //responseCancelRequestClientOrder();
        FixEvaluationData data = new FixEvaluationData(cancelRequest,sessionID);
        EventDispatcher.getInstance().dispatchEvent(EventType.CANCEL_ORDER_REQUEST, data);
    }

    public static void handlePauseResumeRequest(OrderPauseResumeRequest pauseResumeRequest,SessionID sessionID)  {
        //responseCancelRequestClientOrder();
        FixEvaluationData data = new FixEvaluationData(pauseResumeRequest,sessionID);
        EventDispatcher.getInstance().dispatchEvent(EventType.PAUSE_RESUME, data);
    }

    public static void handleCancelRejected(OrderCancelReject cancelRejected,SessionID sessionID)  {
        //responseCancelRequestClientOrder();
        FixEvaluationData data = new FixEvaluationData(cancelRejected,sessionID);
        EventDispatcher.getInstance().dispatchEvent(EventType.CANCEL_REJECTED, data);
    }

    public static void handelPartialFillClientOrder(ClientOrder clientOrder,ExecutionReport triggerReport) throws Exception{
        OrdStatus ordStatus = new OrdStatus(OrdStatus.PARTIALLY_FILLED);
        ExecType execType = new ExecType(ExecType.PARTIAL_FILL);
        if(triggerReport==null){
            throw new IllegalArgumentException("Trigger report can't be null when handling partial fill client order error! ");
        }

        double lastPrice = triggerReport.getLastPx().getValue();
        double lasFillQty = triggerReport.getLastShares().getValue();


        //response fill response
        ExecutionReport partialFillReport = (ExecutionReport)triggerReport.clone();

        partialFillReport.set(ordStatus);
        partialFillReport.set(execType);
        partialFillReport.set(new CumQty(clientOrder.getCumQty()));
        partialFillReport.set(new OrderQty(clientOrder.getOrderQty()));
        partialFillReport.set(new LeavesQty(clientOrder.getLeavesQty()));
        partialFillReport.set(new ClOrdID(clientOrder.getClientOrderId()));
        partialFillReport.set(new LastPx(lastPrice));
        partialFillReport.set(new LastShares(lasFillQty));
        sendMessage(partialFillReport,clientOrder.getSessionID(),CLIENT_IN_PARTIAL_FILL,clientOrder.getClientOrderId());
    }

    public static void handelFillClientOrder(ClientOrder clientOrder,ExecutionReport triggerReport) throws Exception{
        OrdStatus ordStatus = new OrdStatus(OrdStatus.FILLED);
        ExecType execType = new ExecType(ExecType.FILL);
        if(triggerReport==null){
            throw new IllegalArgumentException("Trigger report can't be null when handling partial fill client order error! ");
        }

        double lastPrice = triggerReport.getLastPx().getValue();
        double lasFillQty = triggerReport.getLastShares().getValue();

        //response fill response
        ExecutionReport fillReport = (ExecutionReport)triggerReport.clone();

        fillReport.set(ordStatus);
        fillReport.set(execType);
        fillReport.set(new CumQty(clientOrder.getCumQty()));
        fillReport.set(new OrderQty(clientOrder.getOrderQty()));
        fillReport.set(new LeavesQty(clientOrder.getLeavesQty()));
        fillReport.set(new ClOrdID(clientOrder.getClientOrderId()));
        fillReport.set(new LastPx(lastPrice));
        fillReport.set(new LastShares(lasFillQty));
        sendMessage(fillReport,clientOrder.getSessionID(),CLIENT_IN_FILL,clientOrder.getClientOrderId());
    }

    public static void responseClientOrderWhenCompleted(ClientOrder clientOrder,ExecutionReport report) throws Exception {
        String logMsg = "";
        ExecType execType = new ExecType(ExecType.FILL);
        switch (clientOrder.getOrdStatus().getValue()){
            case OrdStatus.FILLED:
                logMsg = CLIENT_IN_FILL;
                execType= new ExecType(ExecType.FILL);
                break;
            case OrdStatus.REJECTED:
                logMsg = CLIENT_IN_REJECTED;
                execType= new ExecType(ExecType.REJECTED);
                break;
            case OrdStatus.EXPIRED:
                logMsg = CLIENT_IN_EXPIRED;
                execType= new ExecType(ExecType.EXPIRED);
                break;
            case OrdStatus.CANCELED:
                logMsg = CLIENT_IN_REPORT_CANCELED;
                execType= new ExecType(ExecType.CANCELED);
                break;
        }

        report.set(new OrderID(clientOrder.getClientOrderId()));
        report.set(new ExecID(UUID.randomUUID().toString()));
        report.set(new ExecTransType(ExecTransType.NEW));
        report.set(execType);
        report.set(clientOrder.getOrdStatus());
        report.set( new LeavesQty(clientOrder.getLeavesQty()));
        report.set( new CumQty(clientOrder.getCumQty()));
        report.set( new AvgPx(clientOrder.getPrice()));

        report.set(new ClOrdID(clientOrder.getClientOrderId()));
        sendMessage(report, clientOrder.getSessionID(), logMsg, clientOrder.getClientOrderId());
    }
}
