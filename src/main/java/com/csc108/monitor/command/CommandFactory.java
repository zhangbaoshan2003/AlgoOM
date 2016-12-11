package com.csc108.monitor.command;

import org.drools.spi.Tuple;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/5/25.
 */
public class CommandFactory {
    private HashMap<String,HashMap<String,CommandBase>> commandDic = new HashMap<>();

    private static CommandFactory instance = new CommandFactory();
    public static CommandFactory getInstance(){
        return instance;
    }
    private CommandFactory(){
        HashMap<String,CommandBase> secCommands;
        //FixListSessionsCommand listSessionsCommand = new FixListSessionsCommand();
        FixEngineCommand fixEngineCommand= new FixEngineCommand();
        secCommands = new HashMap<>();
        secCommands.putIfAbsent(fixEngineCommand.getSecondLevelKey(),fixEngineCommand);
        commandDic.putIfAbsent("fixModel",secCommands);

        CommandBase clientOrderCommand= new ClientOrderCommand();
        secCommands = new HashMap<>();
        secCommands.putIfAbsent(clientOrderCommand.getSecondLevelKey(),clientOrderCommand);
        commandDic.putIfAbsent(clientOrderCommand.getFirstLevelKey(),secCommands);

        CommandBase cmd= new ExchangeOrderCommand();
        secCommands = new HashMap<>();
        secCommands.putIfAbsent(cmd.getSecondLevelKey(),cmd);
        commandDic.putIfAbsent(cmd.getFirstLevelKey(),secCommands);

        cmd= new DataCommand();
        secCommands = new HashMap<>();
        secCommands.putIfAbsent(cmd.getSecondLevelKey(),cmd);
        commandDic.putIfAbsent(cmd.getFirstLevelKey(),secCommands);

        cmd= new TradingRuleCommand();
        secCommands = new HashMap<>();
        secCommands.putIfAbsent(cmd.getSecondLevelKey(),cmd);
        commandDic.putIfAbsent(cmd.getFirstLevelKey(),secCommands);
    }

    public String runCommand(String[] args){
        try{
            if(args.length<3)
                return "At least 3 parameters must be provided!";

            CommandBase cmd = commandDic.get(args[0]).get(args[1]);
            if(cmd==null)
                throw new Exception("Unrecognized command:"+args[0]+" "+args[1]);

            Method[] methods= cmd.getClass().getMethods();
            Method invokeMethod=null;
            for(Method m:methods){
                if(m.getName().equals(args[2])){
                    invokeMethod = m;
                    break;
                }
            }
            if(invokeMethod==null)
                throw new Exception("Unrecognized method:"+args[0]+" "+args[1]+" "+args[2]);

            String result= invokeMethod.invoke(cmd,new Object[]{args} ).toString();
            return result;

        }catch (Exception ex){
            return ex.toString();
        }


    }
}
