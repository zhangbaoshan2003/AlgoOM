package com.csc108.model.fix;

import com.csc108.configuration.GlobalConfig;
import com.csc108.exceptions.NoAvailableSessionException;
import com.csc108.log.LogFactory;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.utility.Alert;
import quickfix.SessionID;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2015/12/20.
 */
public class SessionPool {
    private static SessionPool instance;
    public static SessionPool getInstance(){
        if(instance==null){
            instance=new SessionPool();
        }
        return instance;
    }

    private SessionID counterSession;
    public SessionID getCounterSession(){
        return counterSession;
    }

    private SessionPool(){
        clientSessions= new ArrayList<>();
        exchangeSessions= new ArrayList<>();
    }
    private ArrayList<SessionID> clientSessions;
    private ArrayList<SessionID> exchangeSessions;

    public ArrayList<SessionID> getClientSessions() {
        return clientSessions;
    }

    public synchronized void addClientSession(SessionID sessionID){
        try {
            Optional<SessionID> existedSessionId= SessionPool.getInstance().getClientSessions().stream().filter(x->x.toString().equals(sessionID.toString()))
                    .findAny();
            if(existedSessionId.isPresent()==false){
                SessionPool.getInstance().getClientSessions().add(sessionID);
                LogFactory.info("The client application logon as :" + sessionID);
            }else{
                LogFactory.info("The client session ID:" + sessionID+" already existed!");
            }
            Alert.clearAlert(String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()));
        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logon client as "+sessionID,ex);
        }
    }

    public synchronized void removeClientSession(SessionID sessionID){
        try {
            SessionPool.getInstance().getClientSessions().remove(sessionID);
            LogFactory.info("Acceptor application logout as :" + sessionID);
            Alert.fireAlert(Alert.Severity.Critical,
                    String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()), sessionID.toString() + " logged out !", null);

        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logout acceptor as "+sessionID,ex);
        }
    }

    public synchronized void addExchangeSession(SessionID sessionID){
        try {
            if(sessionID.getTargetCompID().equals(GlobalConfig.getCounterCompId())==true ){
                counterSession = sessionID;
            }else{
                Optional<SessionID> existedSessionId= SessionPool.getInstance().getExchangeSessions().stream().filter(x->x.toString().equals(sessionID.toString()))
                        .findAny();
                if(existedSessionId.isPresent()==false){
                    SessionPool.getInstance().getExchangeSessions().add(sessionID);
                    LogFactory.info("The exchange application logon as :" + sessionID);
                }else{
                    LogFactory.info("The exchange session ID:" + sessionID+" already existed!");
                }
            }
            Alert.clearAlert(String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()));

        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logon exchange as "+sessionID,ex);
        }
    }

    public synchronized void removeExchangeSession(SessionID sessionID){
        try {
            SessionPool.getInstance().getExchangeSessions().remove(sessionID);
            LogFactory.info("Acceptor application logout as :" + sessionID);
            Alert.fireAlert(Alert.Severity.Critical,
                    String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()), sessionID.toString() + " logged out !", null);

        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logout acceptor as "+sessionID,ex);
        }
    }

    public ArrayList<SessionID> getExchangeSessions() {
        return exchangeSessions;
    }

    private AtomicInteger counter= new AtomicInteger(0) ;
    public SessionID pickupExchangeSessionID(OrderHandler orderHandler) throws Exception {
        //pegging order, routed to counter directly
        if(orderHandler.isPeggingOrder()==true){
            return counterSession;
        }

        if(exchangeSessions.size()==0)
            return null;

        //algo order, robbin ring algo session
        if(exchangeSessions.size()==1)
            return exchangeSessions.get(0);

        //robbin ring pick up exchange sessions
        int index = (counter.getAndIncrement())%exchangeSessions.size();
        SessionID sessionID =exchangeSessions.get(index);
        if(sessionID.getTargetCompID().equals(GlobalConfig.getCounterCompId())){
            throw new IllegalArgumentException("Can't use counter session for a algo order!");
        }
        return sessionID;
    }
}
