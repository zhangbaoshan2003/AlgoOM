package com.csc108.log;

import java.util.HashMap;

/**
 * Created by zhangbaoshan on 2015/10/29.
 */
public class LogColorDictinoanry {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static final String NEW_ORDER_REQUEST_FROM_CLIENT="CLIENT >> New Single Order:";
    private static final String NEW_ORDER_REQUEST_FROM_CXG="CLIENT >> New Single Order:";
    private static final String CANCEL_REQUEST_FROM_CLIENT="CLIENT >> New Single Order:";
    private static final String CANCEL_REQUEST_FROM_CHG="CLIENT >> New Single Order:";

    private static final String PENDING_CANCEL_RESPONSE_TO_CLIENT="CLIENT >> New Single Order:";
    private static final String PENDING_NEW__RESPONSE_TO_CLIENT="CLIENT >> New Single Order:";
    private static final String PARTIAL_FILL_RESPONSE_FROM_CHG="CLIENT >> New Single Order:";
    private static final String PARTIAL_FILL_RESPONSE_TO_CLIENT="CLIENT >> New Single Order:";
    private static final String Fill_RESPONSE_to_client="CLIENT >> New Single Order:";
    private static final String FILL_RESPONSE_FROM_EHG="CLIENT >> New Single Order:";
    private static final String CACELED_RESPONSE_FROM_CHG="CLIENT >> New Single Order:";
    private static final String CACELED_RESPONSE_TO_CLIENT="CLIENT >> New Single Order:";
    private static final String CACELE_REJECT_RESPONSE_FROM_CHG="CLIENT >> New Single Order:";
    private static final String CACELED_REJECT_RESPONSE_TO_CLIENT="CLIENT >> New Single Order:";



    private static HashMap<String,String> colorDic;

    static {
        colorDic=new HashMap<>();
        colorDic.put(NEW_ORDER_REQUEST_FROM_CLIENT,ANSI_YELLOW);
        colorDic.put(NEW_ORDER_REQUEST_FROM_CXG,ANSI_CYAN);
        colorDic.put(CANCEL_REQUEST_FROM_CLIENT,ANSI_PURPLE);
        colorDic.put(CANCEL_REQUEST_FROM_CHG,ANSI_WHITE);

    }
}
