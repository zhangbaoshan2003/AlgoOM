package com.csc108.monitor.command;

import com.csc108.log.LogFactory;
import com.csc108.utility.PrintTable;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import quickfix.Session;
import quickfix.SessionID;

/**
 * Created by zhangbaoshan on 2016/5/25.
 */
public class CommandBase {
    protected Options options = new Options();
    protected CommandLineParser parser = new DefaultParser();

    public String getFirstLevelKey(){
        return "N/A";
    }
    public String getSecondLevelKey(){
        return "N/A";
    }

    protected void generateRow(SessionID sessionID,PrintTable table){
        Session session= null;
        int numOfRows = table.get_numRows();
        try{
            session= Session.lookupSession(sessionID);
        }catch (Exception ex){
            LogFactory.error("Error happend when lookup session @ "+sessionID,ex);
        }

        if(session!=null){
            table.addString(numOfRows ,0,"True");
            table.addString(numOfRows, 1, Boolean.toString(session.getRefreshOnLogon()) );
            table.addString(numOfRows ,2,session.getSessionID().getSenderCompID()+" -> "+session.getSessionID().getTargetCompID());
        }else{
            table.addString(numOfRows ,0,"False");
            table.addString(numOfRows, 1, "False" );
            table.addString(numOfRows ,2,sessionID.getSenderCompID()+" -> "+sessionID.getTargetCompID());
        }
    }

    public String run(String[] args) throws Exception  {
        return "N/A";
    }
}
