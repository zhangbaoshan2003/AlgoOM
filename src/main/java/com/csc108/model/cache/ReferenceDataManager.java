package com.csc108.model.cache;

import com.csc108.log.LogFactory;
import com.csc108.model.data.SessionGroup;
import com.csc108.model.data.TradingSession;
import com.csc108.model.fixModel.order.OrderHandler;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NIUXX on 2016/12/11.
 */
public class ReferenceDataManager  {
    private Set<LocalTime> anchorTimes =null;
    public static ReferenceDataManager instance  = new ReferenceDataManager();
    private ReferenceDataManager(){
        anchorTimes = new HashSet();
        anchorTimes.add(LocalTime.of(9, 30, 0));
        //anchorTimes.add(LocalTime.of(11, 30, 0));
        anchorTimes.add(LocalTime.of(13, 0, 0));
        //anchorTimes.add(LocalTime.of(15, 00, 0));
    }

    public static ReferenceDataManager getInstance(){
        return instance;
    }

    private TradingSessionGroupCache tradingSessionGroupCache = new TradingSessionGroupCache();

    public void init() throws Exception{
        try{
            tradingSessionGroupCache.init();
        }catch (Exception ex){
            LogFactory.error("Failed to initialize trading session group cache",ex);
            throw ex;
        }
    }

    public void validateNewOrder(OrderHandler orderHandler) throws Exception {
        String symbol = orderHandler.getClientOrder().getSecurityId();
        SessionGroup sessionGroup = tradingSessionGroupCache.get(TradingSession.symbol2SessionGroup(symbol));

        if(sessionGroup.isTradable(orderHandler.getTransactionTime().toLocalTime())==false){
            throw new IllegalArgumentException(String.format("Order[%s]'s transaction time @ [%s] is not in any tradable session!",orderHandler.getClientOrder().getClientOrderId(),
                    orderHandler.getTransactionTime()));
        }

        if (orderHandler.getClientOrder().getEffectiveTime().isAfter(orderHandler.getClientOrder().getExpireTime())) {
            throw new IllegalArgumentException(String.format("Order[%s]'s effective time @ [%s] is after the expire time [%s]!",
                    orderHandler.getClientOrder().getClientOrderId(),
                    orderHandler.getClientOrder().getEffectiveTime(),
                    orderHandler.getClientOrder().getExpireTime()));
        }

        if (orderHandler.getClientOrder().getExpireTime().isBefore(orderHandler.getTransactionTime())) {
            throw new IllegalArgumentException(String.format("Order[%s]'s expire time @ [%s] is before the transaction time [%s]!",
                    orderHandler.getClientOrder().getClientOrderId(),
                    orderHandler.getClientOrder().getExpireTime(),
                    orderHandler.getTransactionTime()));
        }

        if(sessionGroup.isTradable(orderHandler.getClientOrder().getExpireTime().toLocalTime())==false ){
            if( anchorTimes.contains(orderHandler.getClientOrder().getExpireTime().toLocalTime())==false){
                throw new IllegalArgumentException(String.format("Order[%s]'s expire time @ [%s] is not in any tradable session!", orderHandler.getClientOrder().getClientOrderId(),
                        orderHandler.getClientOrder().getExpireTime()));
            }
        }

        if(sessionGroup.isTradable(orderHandler.getClientOrder().getExpireTime().toLocalTime())==false ){
            throw new IllegalArgumentException(String.format("Order[%s]'s expire time @ [%s] is not in any tradable session!", orderHandler.getClientOrder().getClientOrderId(),
                    orderHandler.getClientOrder().getExpireTime()));
        }

        if(sessionGroup.isTradable(orderHandler.getClientOrder().getEffectiveTime().toLocalTime())==false ){
            if( anchorTimes.contains(orderHandler.getClientOrder().getEffectiveTime().toLocalTime())==false){
                throw new IllegalArgumentException(String.format("Order[%s]'s effective time @ [%s] is not in any tradable session!", orderHandler.getClientOrder().getClientOrderId(),
                        orderHandler.getClientOrder().getEffectiveTime()));
            }
        }
    }

    public boolean isInTradibleSession(OrderHandler orderHandler){
        String symbol = orderHandler.getClientOrder().getSecurityId();
        SessionGroup sessionGroup = tradingSessionGroupCache.get(TradingSession.symbol2SessionGroup(symbol));
        return sessionGroup.isTradable(orderHandler.getTransactionTime().toLocalTime());
    }

    private double DEFAULT_LOT_SIZE=100.0;
    public double getLotSize(String securityId){
        return DEFAULT_LOT_SIZE;
    }

    private int rejectedTimesBeforeFrozen=20;
    public int getRejectedTimesBeforeFrozen(){
        return rejectedTimesBeforeFrozen;
    }

}
