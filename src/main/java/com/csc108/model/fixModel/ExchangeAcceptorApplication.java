package com.csc108.model.fixModel;


import com.csc108.log.LogFactory;
import com.csc108.model.fixModel.sessionPool.SessionPool;
import quickfix.*;
import quickfix.field.Text;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.OrderCancelReject;

/**
 * Created by LEGEN on 2016/1/30.
 */
public class ExchangeAcceptorApplication extends MessageCracker implements Application {

    public ExchangeAcceptorApplication() {

    }

    public void onCreate(SessionID sessionID) {
        LogFactory.info("Acceptor session " + sessionID + " created!");
    }

    public void onLogon(SessionID sessionID) {
        try {
            SessionPool.getInstance().getAlgoExchangeSessions().add(sessionID);
            LogFactory.info("Acceptor application logon as :" + sessionID);
        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logon down acceptor as "+sessionID,ex);
        }

    }

    public void onLogout(SessionID sessionID) {
        try {
            SessionPool.getInstance().getAlgoExchangeSessions().remove(sessionID);
            LogFactory.info("Acceptor application logout as :" + sessionID);
        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logout down acceptor as "+sessionID,ex);
        }
    }

    public void toAdmin(Message message, SessionID sessionID) {

    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        LogFactory.info("fromAdmin:" + message);
    }

    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        com.csc108.log.LogFactory.debug("Down acceptor incoming " + "@" + Thread.currentThread().getId() + "@" + sessionID+"#"+message);
        if(message instanceof ExecutionReport){
            try {

                try{
                    if(((ExecutionReport) message).isSetText()){
                        Text text = new Text();
                        byte[] bt= message.getField(text).getValue().getBytes("ISO-8859-1");
                        String context = new String(bt, "UTF-8");
                        com.csc108.log.LogFactory.debug("Text is:" + context);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    System.out.println("Error for parse text:"+ex);
                }
            }catch (Exception ex){
                ex.printStackTrace();
                com.csc108.log.LogFactory.error("Initiator from app error",ex);
            }
        } else if(message instanceof OrderCancelReject){
            try {
                try{
                    if(((OrderCancelReject) message).isSetText()){
                        Text text = new Text();
                        byte[] bt= message.getField(text).getValue().getBytes("ISO-8859-1");
                        String context = new String(bt, "UTF-8");
                        com.csc108.log.LogFactory.debug("Text is:" + context);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    System.out.println("Error for parse text:"+ex);
                }
            }catch (Exception ex){
                ex.printStackTrace();
                com.csc108.log.LogFactory.error("Initiator from app error",ex);
            }
        }
        else{
            com.csc108.log.LogFactory.error("Initiator can't handel incoming message:"+message,null);
        }
    }
}