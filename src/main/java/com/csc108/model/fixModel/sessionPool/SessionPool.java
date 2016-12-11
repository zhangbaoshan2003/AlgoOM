package com.csc108.model.fixModel.sessionPool;

import com.csc108.log.LogFactory;
import com.csc108.utility.Alert;
import quickfix.SessionID;

import java.util.ArrayList;
import java.util.Optional;
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

    private SessionPool(){
        clientSessions= new ArrayList<>();
        algoExchangeSessions= new ArrayList<>();
        peggingExchangeSessions = new ArrayList<>();
    }

    private ArrayList<SessionID> clientSessions;
    private ArrayList<SessionID> algoExchangeSessions;
    private ArrayList<SessionID> peggingExchangeSessions;

    public ArrayList<SessionID> getClientSessions() {
        return clientSessions;
    }

    public ArrayList<SessionID> getPeggingExchangeSessions() {
        return peggingExchangeSessions;
    }

    public synchronized void addClientSession(SessionID sessionID){
        try {
            Optional<SessionID> existedSessionId= this.getClientSessions().stream().filter(x->x.toString().equals(sessionID.toString()))
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
            this.getClientSessions().remove(sessionID);
            this.getClientSessions().remove(sessionID);
            LogFactory.info("Acceptor application logout as :" + sessionID);
            Alert.fireAlert(Alert.Severity.Critical,
                    String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()), sessionID.toString() + " logged out !", null);

        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logout acceptor as "+sessionID,ex);
        }
    }

    public synchronized void addExchangeSession(SessionID sessionID){
        try {
            if(sessionID.getTargetCompID().contains("PEG") ){
                Optional<SessionID> existedSessionId= SessionPool.getInstance().getPeggingExchangeSessions()
                        .stream().filter(x->x.toString().equals(sessionID.toString()))
                        .findAny();
                if(existedSessionId.isPresent()==false){
                    SessionPool.getInstance().getPeggingExchangeSessions().add(sessionID);
                    LogFactory.info("The pegging exchange application logon as :" + sessionID);
                }else{
                    LogFactory.info("The pegging exchange session ID:" + sessionID+" already existed!");
                }
            }else{
                Optional<SessionID> existedSessionId= SessionPool.getInstance().getAlgoExchangeSessions().stream().filter(x->x.toString().equals(sessionID.toString()))
                        .findAny();
                if(existedSessionId.isPresent()==false){
                    SessionPool.getInstance().getAlgoExchangeSessions().add(sessionID);
                    LogFactory.info("The algo exchange application logon as :" + sessionID);
                }else{
                    LogFactory.info("The algo exchange session ID:" + sessionID+" already existed!");
                }
            }
            Alert.clearAlert(String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()));

        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logon exchange as "+sessionID,ex);
        }
    }

    public synchronized void removeExchangeSession(SessionID sessionID){
        try {
            boolean algoLoggedout=  SessionPool.getInstance().getAlgoExchangeSessions().remove(sessionID);
            boolean peggingLoggedOut = SessionPool.getInstance().getAlgoExchangeSessions().remove(sessionID);

            if (algoLoggedout){
                LogFactory.info("Algo exchange session logout as :" + sessionID);
            }else{
                LogFactory.info("Pegging exchange session logout as :" + sessionID);
            }

            Alert.fireAlert(Alert.Severity.Critical,
                    String.format(Alert.SESSION_CONNECTION_ERROR, sessionID.toString()), sessionID.toString() + " logged out !", null);

        }catch (Exception ex){
            com.csc108.log.LogFactory.error("Error happened when logout acceptor as "+sessionID,ex);
        }
    }

    public ArrayList<SessionID> getAlgoExchangeSessions() {
        return algoExchangeSessions;
    }

    private AtomicInteger algoCounter= new AtomicInteger(0) ;
    public SessionID pickupAlgoExchangeSessionID() {
        if(getAlgoExchangeSessions().size()==0)
            return null;

        int index = (algoCounter.getAndIncrement())%getAlgoExchangeSessions().size();
        SessionID sessionID =getAlgoExchangeSessions().get(index);
        return sessionID;
    }

    private AtomicInteger peggingCounter= new AtomicInteger(0) ;
    public SessionID pickupPeggingExchangeSessionID() {
        if(getPeggingExchangeSessions().size()==0)
            return null;

        int index = (peggingCounter.getAndIncrement())%getPeggingExchangeSessions().size();
        SessionID sessionID =getPeggingExchangeSessions().get(index);
        return sessionID;
    }
}
