package com.csc108.model.fix;

import com.csc108.model.IEvaluationData;
import quickfix.Message;
import quickfix.SessionID;

/**
 * Created by LEGEN on 2016/5/14.
 */
public class FixEvaluationData implements IEvaluationData {
    private final Message fixMsg;
    private final SessionID sessionID;

    public FixEvaluationData(Message fixMsg, SessionID sessionID){
        this.fixMsg = fixMsg;
        this.sessionID = sessionID;
    }

    public Message getFixMsg(){
        return fixMsg;
    }

    public SessionID getSessionID(){
        return sessionID;
    }
}
