package com.csc108.monitor.command;

import com.csc108.log.LogFactory;
import com.csc108.model.fix.SessionPool;
import com.csc108.utility.PrintTable;
import org.apache.activemq.command.Command;
import org.apache.commons.cli.CommandLine;
import quickfix.Session;
import quickfix.SessionID;

/**
 * Created by zhangbaoshan on 2016/5/27.
 */
public class FixEngineCommand extends CommandBase {

    @Override
    public String getFirstLevelKey(){
        return "fix";
    }

    @Override
    public String getSecondLevelKey(){
        return "engine";
    }

    public String list_all_sessions(String[] arg){
        try {
            StringBuffer sb= new StringBuffer();

            PrintTable formattedTable = new PrintTable();
            formattedTable.addString(0, 0, "IsLoggedOn");
            formattedTable.addString(0, 1, "IsResetOnLogOn");
            formattedTable.addString(0, 2, "Session");

            SessionPool.getInstance().getClientSessions().stream()
                    .filter(x -> x != null)
                    .forEach(x -> generateRow(x, formattedTable));

            SessionPool.getInstance().getExchangeSessions().stream()
                    .filter(x -> x != null)
                    .forEach(x -> generateRow(x, formattedTable));

            if(SessionPool.getInstance().getCounterSession()!=null){
                generateRow(SessionPool.getInstance().getCounterSession(),formattedTable);
            }

            return formattedTable.toString();
        }catch (Exception ex){
            LogFactory.error("list_all_sessions error!",ex);
            return  "Error happpend when processing list_all_sessions!"+ex;
        }
    }
}
