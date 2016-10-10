package com.csc108.model.fix.order;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
public class OrderPool {
    private static final ConcurrentHashMap<String,ClientOrder> clientOrderMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,ExchangeOrder> exchangeOrderMap= new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,ManuallyOrder> manuallyOrderMap= new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String,ClientOrder> getClientOrderMap(){
        return clientOrderMap;
    }

    public static ConcurrentHashMap<String,ExchangeOrder> getExchangeOrderMap(){
        return exchangeOrderMap;
    }

    public static final ConcurrentHashMap<String,ManuallyOrder> getManuallyOrderMap(){
        return manuallyOrderMap;
    }

}
