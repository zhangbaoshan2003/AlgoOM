package com.csc108.utility;

import com.csc108.configuration.GlobalConfig;
import com.csc108.log.LogFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by zhangbaoshan on 2016/5/11.
 */
public class Alert {
    public static final String SESSION_NOT_FOUND_KEY="Session %s not found for order %s";
    public static final String FIELD_NOT_FOUND_KEY="%s not defined for order %s";
    public static final String SENDING_MSG_ERROR="Sending fixModel msg failed for order %s";
    //public static final String PROCESS_ORDER_ERROR="Process %s error";
    public static final String SEND_OUT_EXCHANGE_ORDER_ERROR_KEY="Send out exg order %s error";
    public static final String REJECT_CANCEL_ERROR_KEY="Reject order %s cancel error";
    public static final String DIRECT_CANCEL_ORDER_ERROR_KEY ="Cancel order %s directly error";
    public static final String CANCEL_EXG_ORDER_ERROR_KEY="Cancel Exg order %s error";
    public static final String INITIALIZE_CLIENT_ORDER_KEY="Initialize order %s error";
    public static final String NEW_HANDLE_ERROR="New handel order %s error";
    public static final String EVALUATION_HANDLER_ERROR="Evaluation handle %s error";

    public static final String PROCESS_EXECUTION_REPORT_ERROR="Process execution report %s error";
    public static final String PROCESS_CANCEL_REJECT_ERROR="Process cancel reject %s error";

    public static final String INIT_MARKET_DATA_MQ_ERROR="Initialize market data active mq error";
    public static final String SUBCRIBE_MARKET_DATA_MQ_ERROR= "Subscribe security %s error!";

    public static final String SESSION_CONNECTION_ERROR="Session [%s] disconnected!";
    public static final String MARKET_DATA_SUBCRIBE_ERROR="Order [%s] subscribe market data error!";
    public static final String PROCESS_ORDER_ERROR ="Order %s not processed!";

    public static final String MARKET_DATA_UPDATE_ERROR ="Order %s market data updated error!";

    public static final String PROCESS_CONDITION_ORDER_ERROR ="Conditional Order %s evaluate condition error!";

    public static final String PAUSE_RESUME_ERROR="order %s failed to pause/resume";

    public static final String PAUER_ERROR="Order [%s] failed to be paused!";
    public static final String RESUME_ERROR="Order [%s] failed to be resume!";

    public static final String PAUSE_ORDER_INFO="order %s has been paused!";
    public static final String RESUME_ORDER_INFO="order %s has been resumed!";

    public static final String PEG_NO_ALLOCATION_ERROR="order %s is not allocated!";

    public static final String VALIDATE_ORDER_ERROR = "order [%s] is not valid!";


    private static Connection con;
    private static Session session;
    private static Destination destination;
    private static MessageProducer producer;
    private static Topic topic;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static HashMap<String,Boolean> alertFlagContainer = new HashMap<>();

    public static String Topic="ALERT.OM.BUSINESS";

    public static void Init(){
        ActiveMQConnectionFactory connectionFactory=new ActiveMQConnectionFactory();
    }

    public enum Severity {
        Critical,
        Major,
        Minor,
        Fatal,
        Info,
        Clear
    }

    private static void sendToMq(String msg){

    }

    public static void fireAlert(Severity severity_, String key_, String msg_,Exception ex) {
        if(GlobalConfig.isActive_mq_available()==true){
            HashMap<String,String> map = new HashMap<>();
            map.put("AlertID",key_);
            map.put("Severity",severity_.toString());
            map.put("Time", sdf.format(new Date()));
            map.put("Instance", String.format("%s:%s", GlobalConfig.getMonitorIP(),Integer.toString(GlobalConfig.getMonitorPort())));
            map.put("Message", msg_);
            AlertManager.getInstance().sendMsg(map);

            synchronized (alertFlagContainer){
                alertFlagContainer.put(key_, true);
            }

        }

        if(ex!=null)
            LogFactory.error("Alert exception",ex);


        if(severity_==Severity.Critical){
            LogFactory.error(msg_, ex);
        }else if (severity_==Severity.Fatal){
            LogFactory.error(msg_,ex);
        }else if (severity_==Severity.Major){
            if(ex==null){
                LogFactory.warn(key_+":"+ msg_);
            }else{
                LogFactory.warn(key_+":"+ msg_+"\n"+ex.getStackTrace());
            }
        }else if (severity_==Severity.Info){
            LogFactory.info(key_ + ":" + msg_);
        }else if (severity_==Severity.Minor){
            if(ex==null){
                LogFactory.warn(key_+":"+ msg_);
            }else{
                LogFactory.warn(key_+":"+ msg_+"\n"+ex.getStackTrace());
            }

        }
    }

    public static void clearAlert( String key_) {
        synchronized (alertFlagContainer){
            if(alertFlagContainer.get(key_)!=null){
                if(alertFlagContainer.get(key_)==Boolean.TRUE){
                    alertFlagContainer.put(key_,false);
                }else{
                    return;
                }
            }
        }

        HashMap<String,String> map = new HashMap<>();
        map.put("AlertID",key_);
        map.put("Severity", Severity.Clear.toString());
        map.put("Time", sdf.format(new Date()));
        map.put("Instance", String.format("%s:%s", GlobalConfig.getMonitorIP(),Integer.toString(GlobalConfig.getMonitorPort())));
        map.put("Message", " ");
        AlertManager.getInstance().sendMsg(map);
    }

}
