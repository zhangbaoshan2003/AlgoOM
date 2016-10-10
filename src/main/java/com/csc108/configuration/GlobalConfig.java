package com.csc108.configuration;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Created by zhangbaoshan on 2015/12/15.
 */
public class GlobalConfig {
    private static String configPath="configuration/Config.properties";
    private static boolean ifUseDisruptorBlockingQueue=true;
    private static int threadNums;
    private static boolean ifApplyActiveMq;
    private static int bufferSize;
    private static boolean releaseMode =false;
    private static boolean outputDebugInfo=false;
    private static int NumOfChildOrders;

    private static String[] newSingleOrderRuleFiles;
    private static String[] cancelRequestRuleFiles;
    private static String[] executionReportRuleFiles;
    private static String[] cancelRejectRuleFiles;

    private static String monitorIP;
    private static int monitorPort;
    private static boolean reverseConnection;

    private static String activeMqUrl;
    private static String activeMqUser;
    private static String activeMqPassword;
    private static String realTimeDataMqUrl;
    private static String tradeDataMqUrl;

    private static int activeMqTimeout;
    private static String alertMqUrl;

    private static int timeOutInMinutes;
    private static int wakeupInterval;

    private static boolean active_mq_available;
    private static int maxNumOfActiveMqConnections;

    private static String counterCompId;


    public static void setupReleaseMode(boolean value){
        releaseMode=value;
    }

    static {
        Properties pro=new Properties();
        File conFile = new File(configPath);
        try {
            FileReader fileReader = new FileReader(conFile);
            pro.load(fileReader);
        }catch (Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }

        ifApplyActiveMq = Boolean.parseBoolean(pro.get("IfApplyActiveMq").toString());
        threadNums = Integer.parseInt(pro.get("ThreadNum").toString());
        ifUseDisruptorBlockingQueue = (Integer.parseInt(pro.get("BlockingQueueImp").toString()) ==1);
        bufferSize = Integer.parseInt(pro.get("DisruptorBlockingQueueBufferSize").toString());
        releaseMode = Boolean.parseBoolean(pro.get("ReleaseMode").toString());
        outputDebugInfo = Boolean.parseBoolean(pro.get("OutputDebugInfo").toString());
        NumOfChildOrders = Integer.parseInt(pro.get("NumOfChildOrders").toString());

        newSingleOrderRuleFiles = pro.getProperty("NewSingleOrderRule").split(";");
        cancelRequestRuleFiles = pro.getProperty("CancelRequestRule").split(";");
        executionReportRuleFiles = pro.getProperty("ExecutionReportRule").split(";");
        cancelRejectRuleFiles = pro.getProperty("CancelRejectRule").split(";");

        monitorIP = pro.getProperty("MonitorIp");
        monitorPort =  Integer.parseInt(pro.getProperty("MonitorPort"));
        reverseConnection =  Boolean.parseBoolean(pro.get("ReverseConnection").toString());

        //active mq config
        activeMqUrl = pro.getProperty("mqurl");
        activeMqUser = pro.getProperty("mquser");
        activeMqPassword = pro.getProperty("mqpassword");
        activeMqTimeout = Integer.parseInt(pro.getProperty("mqsendtimeout")) ;

        //alert mq config
        alertMqUrl = pro.getProperty("alertMqUrl");
        wakeupInterval = Integer.parseInt(pro.get("wakeupInterval").toString());
        active_mq_available = Boolean.parseBoolean(pro.get("active_mq_available").toString());
        realTimeDataMqUrl = pro.getProperty("realTimeDataMqUrl");

        timeOutInMinutes=Integer.parseInt(pro.get("TimeoutInMinutes").toString());
        maxNumOfActiveMqConnections=Integer.parseInt(pro.get("maxNumOfActiveMqConnections").toString());

        counterCompId = pro.getProperty("counterCompId");
        tradeDataMqUrl= pro.getProperty("tradeDataMqUrl");
    }

    public static int getThreadNums() {
        return threadNums;
    }

    public static boolean ifApplyActiveMq() {
        return ifApplyActiveMq;
    }

    public static boolean ifUseDisruptorBlockingQueue() {
        return ifUseDisruptorBlockingQueue;
    }

    public static int getBufferSize() {
        return bufferSize;
    }

    public static boolean isReleaseMode() {
        return releaseMode;
    }

    public static boolean isOutputDebugInfo() {
        return outputDebugInfo;
    }

    public static int getNumOfChildOrders() {
        return NumOfChildOrders;
    }

    public static void setNumOfChildOrders(int value) {
        NumOfChildOrders=value;
    }

    public static String[] getNewSingleOrderRuleFiles() {
        return newSingleOrderRuleFiles;
    }

    public static String[] getCancelRequestRuleFiles() {
        return cancelRequestRuleFiles;
    }

    public static String[] getExecutionReportRuleFiles() {
        return executionReportRuleFiles;
    }

    public static String[] getCancelRejectRuleFiles() {
        return cancelRejectRuleFiles;
    }

    public static String getMonitorIP() {
        return monitorIP;
    }

    public static int getMonitorPort() {
        return monitorPort;
    }

    public static boolean isReverseConnection() {
        return reverseConnection;
    }

    public static String getActiveMqUrl() {
        return activeMqUrl;
    }

    public static String getActiveMqUser() {
        return activeMqUser;
    }

    public static String getActiveMqPassword() {
        return activeMqPassword;
    }

    public static String getAlertMqUrl() {
        return alertMqUrl;
    }

    public static int getWakeupInterval() {
        return wakeupInterval;
    }

    public static boolean isActive_mq_available() {
        return active_mq_available;
    }

    public static int getTimeOutInMinutes() {
        return timeOutInMinutes;
    }

    public static int getMaxNumOfActiveMqConnections() {
        return maxNumOfActiveMqConnections;
    }

    public static String getRealTimeDataMqUrl() {
        return realTimeDataMqUrl;
    }

    public static String getCounterCompId() {
        return counterCompId;
    }

    //only for testing purpose
    public static void setCounterCompId(String p_counterCompId){
        counterCompId = p_counterCompId;
    }

    public static String getTradeDataMqUrl() {
        return tradeDataMqUrl;
    }
}
