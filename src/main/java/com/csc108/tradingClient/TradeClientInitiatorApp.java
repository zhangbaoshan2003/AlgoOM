package com.csc108.tradingClient;

import com.csc108.model.fixModel.sessionPool.SessionPool;
import quickfix.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.OrderCancelReject;
import quickfix.fix42.OrderPauseResumeRequest;

/**
 * Created by zhangbaoshan on 2016/10/25.
 */
public class TradeClientInitiatorApp extends MessageCracker implements Application {

    public TradeClientInitiatorApp() {

    }

    public void onCreate(SessionID sessionID) {
        com.csc108.log.LogFactory.info("Initiator session " +sessionID + " created!");
    }

    public void onLogon(SessionID sessionID) {
        SessionPool.getInstance().addExchangeSession(sessionID);
    }

    public void onLogout(SessionID sessionID) {
        SessionPool.getInstance().removeExchangeSession(sessionID);
    }

    public void toAdmin(Message message, SessionID sessionID) {
    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
    }

    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }

    public void onMessage(OrderPauseResumeRequest message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {

    }

    public void onMessage(OrderCancelReject message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
        try{

        }catch (Exception ex){
            ex.printStackTrace();
            com.csc108.log.LogFactory.error("Handle cancel rejected error!", ex);
        }
    }

    public void onMessage(ExecutionReport executionReport,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
        try{
            System.out.printf("Report: %s %n",executionReport);
        }catch (Exception ex){
            ex.printStackTrace();
            com.csc108.log.LogFactory.error("Handle execution report error!", ex);
        }
    }

    public void run(){

    }
}