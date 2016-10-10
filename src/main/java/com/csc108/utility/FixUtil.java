package com.csc108.utility;

import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import quickfix.field.OrdStatus;

/**
 * Created by zhangbaoshan on 2016/5/8.
 */
public class FixUtil {
    public static final int CLIENT_ORDER_ID=11;
    public static final int  ORIG_CLIENT_ORDER_ID=41;
    public static final int  ORD_PRICE=44;
    public static final int  CUM_QTY=14;
    public static final int  LAST_PRICE=31;
    public static final int  LAST_SHARES=32;
    public static final int  ORDER_QTY=38;
    public static final int  ORDER_STATUS=39;
    public static final int  LEAVES_QTY=151;
    public static final int  EXEC_TYPE=150;

    public static boolean IsOrderCompleted(OrdStatus ordStatus){
        if(ordStatus.getValue()==OrdStatus.FILLED || ordStatus.getValue()==OrdStatus.REJECTED
                || ordStatus.getValue()==OrdStatus.CANCELED ||ordStatus.getValue()==OrdStatus.EXPIRED ){
            return true;
        }
        return false;
    }

    public static boolean IsClientOrderCompleted(OrderHandler orderManager){
        ClientOrder clientOrder = orderManager.getClientOrder();
        long totalInProgressExchangeOrders = orderManager.getExchangeOrders().stream().filter(x->FixUtil.IsOrderCompleted(x.getOrdStatus())==false)
                .count();
        return totalInProgressExchangeOrders==0;
    }

    public static boolean convertable(OrdStatus from,OrdStatus to){
        if(from.getValue()==OrdStatus.PENDING_CANCEL){
            if(to.getValue()==OrdStatus.CANCELED)
                return true;

            if(to.getValue()==OrdStatus.REJECTED)
                return true;

            if(to.getValue()==OrdStatus.FILLED)
                return true;

            return false;
        }

        return true;

//        if(from.getValue()==OrdStatus.PENDING_NEW){
//            if(to.getValue()==OrdStatus.NEW)
//                return true;
//            if(to.getValue()==OrdStatus.PENDING_CANCEL)
//                return true;
//            if(to.getValue()==OrdStatus.FILLED)
//                return true;
//            if(to.getValue()==OrdStatus.PARTIALLY_FILLED)
//                return true;
//            if(to.getValue()==OrdStatus.REJECTED)
//                return true;
//            if(to.getValue()==OrdStatus.CANCELED)
//                return true;
//            return false;
//        }

//        return true;
    }
}
