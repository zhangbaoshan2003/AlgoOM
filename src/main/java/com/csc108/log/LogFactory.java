package com.csc108.log;

import com.csc108.configuration.GlobalConfig;
import org.apache.commons.io.FileUtils;
import quickfix.fix42.Message;

import java.util.ArrayList;

/**
 * Created by zhangbaoshan on 2015/8/17.
 */
public class LogFactory {
    private static final org.slf4j.Logger _sysLogger = org.slf4j.LoggerFactory.getLogger("mylogger2");
    private static final org.slf4j.Logger _errorLogger = org.slf4j.LoggerFactory.getLogger("mylogger1");
    private static final org.slf4j.Logger _algoOmLogger = org.slf4j.LoggerFactory.getLogger("mylogger4");
    private static final org.slf4j.Logger _warningLog= org.slf4j.LoggerFactory.getLogger("mylogger7");
    private static final org.slf4j.Logger _debugLog= org.slf4j.LoggerFactory.getLogger("mylogger6");
    private static final org.slf4j.Logger _droolLogger= org.slf4j.LoggerFactory.getLogger("mylogger8");
    private static final org.slf4j.Logger _eventLogger= org.slf4j.LoggerFactory.getLogger("mylogger9");
    private static final org.slf4j.Logger _marketDataActiveMqLogger= org.slf4j.LoggerFactory.getLogger("mylogger10");
    private static LogThread logThread;

    static {
        System.out.println("Starting log threads ...");
        logThread = new LogThread();
        logThread.start();
    }

    public static org.slf4j.Logger getMarketDataActiveMqLogger(){
        return _marketDataActiveMqLogger;
    }

    public static org.slf4j.Logger getEventLogger(){
        return _eventLogger;
    }

    public static void info(String logText){
        _sysLogger.info(logText);
    }

    public static void omLog(String logText,String trackID , Message fixMsg){
        String innerMsg = OmFixLogMsgBuilder.buildMemeoMsgStr(logText, trackID, fixMsg);
        _algoOmLogger.info(innerMsg);
    }

    public static void error(String message,Throwable ex){
        _errorLogger.error(message, ex);
        String appendEx="";
        if(ex!=null){
            appendEx=ex.toString();
        }
        System.out.println(LogColorDictinoanry.ANSI_RED + message + appendEx.toString() + LogColorDictinoanry.ANSI_RESET);
    }

    public static void warn(String message){
        _warningLog.info(message);
    }

    public static void droolsLog(String message){
        if (GlobalConfig.isOutputDebugInfo()==true){
            _droolLogger.info(message);
        }
    }

    public static void debug(String message){
        if(GlobalConfig.isOutputDebugInfo()==true){
            _debugLog.info(message);
        }
    }

    public static void logOrder(String clientOrderId, ArrayList<String> logLines){
        NewFilePerOrderLogHandler logTask= new NewFilePerOrderLogHandler(clientOrderId,logLines);
        logThread.enqueueTask(logTask);
    }

}
