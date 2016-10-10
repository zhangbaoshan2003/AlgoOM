package com.csc108.model.fix;

import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.drools.OrderMessage;
import com.csc108.log.LogFactory;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import quickfix.*;
import quickfix.field.ExecType;
import quickfix.field.Text;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReject;
import quickfix.fix42.OrderPauseResumeRequest;

/**
 * Created by Administrator on 2015/12/19.
 */
public class InitiatorApplication  extends MessageCracker implements Application {

    public InitiatorApplication() {

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
        //com.csc108.log.LogFactory.debug("Initiator outgoing " + "@" + Thread.currentThread().getId() + "@" + sessionID+"#"+message);
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
            OrderMessage orderMessage = new OrderMessage(message);
            DroolsUtility.processMessage(message, DroolsType.CANCEL_REJECTED);

            FixMsgHelper.handleCancelRejected(message, sessionID);
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("Handle ca" +
                    "" +
                    "" +
                    "ncel rejected error!", ex);
        }
    }

    public void onMessage(ExecutionReport executionReport,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
        try{
            OrderMessage orderMessage = new OrderMessage(executionReport);
            DroolsUtility.processMessage(orderMessage, DroolsType.EXECUTION_REPORT);

            FixMsgHelper.handleExecutionReport(executionReport, sessionID);
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("Handle execution report error!", ex);
        }
    }
}