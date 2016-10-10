package com.csc108.model.fix;

import com.csc108.drools.DroolsType;
import com.csc108.drools.DroolsUtility;
import com.csc108.drools.OrderMessage;
import com.csc108.log.LogFactory;
import com.csc108.model.fix.order.ExchangeOrder;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import quickfix.*;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.fix42.*;

/**
 * Created by Administrator on 2015/12/19.
 */
public class AcceptorApplication extends MessageCracker implements Application {

    public AcceptorApplication() {

    }

    public void onCreate(SessionID sessionID) {
        LogFactory.info("Acceptor session " + sessionID + " created!");
    }

    public void onLogon(SessionID sessionID) {
        SessionPool.getInstance().addClientSession(sessionID);
    }

    public void onLogout(SessionID sessionID) {
        SessionPool.getInstance().removeClientSession(sessionID);
    }

    public void toAdmin(Message message, SessionID sessionID) {
        LogFactory.info("To Admin:"+message.toString());
    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        LogFactory.info("From Admin:" + message);
    }

    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try{
            crack(message, sessionID);
        }catch (Exception ex){
            LogFactory.error("crack message error!",ex);
        }

    }

    public void onMessage(NewOrderSingle message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
        try{
            FixMsgHelper.handleNewOrderRequest((NewOrderSingle)message,sessionID);
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("handle new single order request error!", ex);
        }
    }

    public void onMessage(OrderCancelRequest message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
        try{
            OrderMessage orderMessage = new OrderMessage(message);
            DroolsUtility.processMessage(orderMessage, DroolsType.CANCEL_REQUEST);

            FixMsgHelper.handleCancelOrderRequest(message, sessionID);
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("handle cancel client order request error!", ex);
        }
    }

    public void onMessage(OrderPauseResumeRequest message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
        try{
            FixMsgHelper.handlePauseResumeRequest(message,sessionID);
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("handle pause_resume client order request error!", ex);
        }
    }
}