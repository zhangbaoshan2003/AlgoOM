package com.csc108.monitor.command;

import com.csc108.log.LogFactory;
import com.csc108.model.fix.sessionPool.SessionPool;
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

            sb.append(String.format("%15s%s%s%n", "******", "Acceptor Sessions", "******")) ;

            PrintTable formattedTable = new PrintTable();
            formattedTable.addString(0, 0, "IsLoggedOn");
            formattedTable.addString(0, 1, "IsResetOnLogOn");
            formattedTable.addString(0, 2, "Session");

            SessionPool.getInstance().getClientSessions().stream()
                    .filter(x -> x != null)
                    .forEach(x -> generateRow(x, formattedTable));
            sb.append(formattedTable.toString());


            sb.append(String.format("%n%n%15s%s%s%n", "******", "Algo Sessions", "******")) ;
            PrintTable formattedTable2 = new PrintTable();
            formattedTable2.addString(0, 0, "IsLoggedOn");
            formattedTable2.addString(0, 1, "IsResetOnLogOn");
            formattedTable2.addString(0, 2, "Session");
            SessionPool.getInstance().getAlgoExchangeSessions().stream()
                    .filter(x -> x != null)
                    .forEach(x -> generateRow(x, formattedTable2));
            sb.append(formattedTable2.toString());

            sb.append(String.format("%n%n%15s%s%s%n", "******", "Pegging Sessions", "******")) ;
            PrintTable formattedTable3 = new PrintTable();
            formattedTable3.addString(0, 0, "IsLoggedOn");
            formattedTable3.addString(0, 1, "IsResetOnLogOn");
            formattedTable3.addString(0, 2, "Session");
            SessionPool.getInstance().getPeggingExchangeSessions().stream()
                    .filter(x -> x != null)
                    .forEach(x -> generateRow(x, formattedTable3));
            sb.append(formattedTable3.toString());

            return sb.toString();

        }catch (Exception ex){
            LogFactory.error("list_all_sessions error!",ex);
            return  "Error happened when processing list_all_sessions!"+ex;
        }
    }
}
