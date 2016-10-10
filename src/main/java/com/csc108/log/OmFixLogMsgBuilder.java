package com.csc108.log;

import quickfix.FieldNotFound;
import quickfix.fix42.Message;

import java.text.SimpleDateFormat;

/**
 * Created by zhangbaoshan on 2015/9/24.
 */
public class OmFixLogMsgBuilder {

    public static final char DEFAULT_DELIMETER = '|';
    public static final char SOH_DELIMETER = (char) 0x01;
    public static final String CALLING_METHOD="Method:";
    public static final String SPLIT_FLAG="#";
    public static final String INCOMMING_FLAG="<<<";
    public static final String OUTGOING_FLAG=">>>";
    public static final String TIME_STR_FLAG="@";

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");

    public static String buildMemeoMsgStr (String logMsg,String trackId, Message fixMsg) {
        try{
            String msg = buildMemeoMsg(logMsg,trackId,fixMsg);
            return msg;
        }catch (Exception ex){
            ex.printStackTrace();
            return "Error parse buildMemeoMsgStr";
        }
    }

    private static String buildMemeoMsg(String logMessage,String trackId, Message fixMsg) throws FieldNotFound {
        String totalLog  = OmFixLogMsgBuilder.TIME_STR_FLAG+logMessage+ OmFixLogMsgBuilder.SPLIT_FLAG+trackId
                + OmFixLogMsgBuilder.SPLIT_FLAG
                +fixMsg.toString();
        return totalLog ;
    }
}
