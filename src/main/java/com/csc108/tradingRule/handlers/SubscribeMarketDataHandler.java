package com.csc108.tradingRule.handlers;

import com.csc108.model.cache.MicroStructureDataManager;
import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.cache.RealTimeDataManager;
import com.csc108.model.fix.order.OrderHandler;
import com.csc108.model.market.MicroStructure;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.utility.Alert;

import java.util.LinkedHashMap;

/**
 * Created by zhangbaoshan on 2016/11/8.
 */
public class SubscribeMarketDataHandler implements IHandler {
    @Override
    public String getHandlerName() {
        return "SubscribeMarketDataHandler";
    }

    @Override
    public void handle(OrderHandler orderHandler, LinkedHashMap<String, String> parameters) {
        if (orderHandler.getClientOrder().getSymbol().isEmpty())
            throw new IllegalArgumentException("No symbol provided for orderbook subscription!");

        if (orderHandler.getClientOrder().getExchangeDest().isEmpty())
            throw new IllegalArgumentException("No exchange destination provided for orderbook subscription!");

        if(orderHandler.getClientOrder().getSecurityId()==null || orderHandler.getClientOrder().getSecurityId().isEmpty() ){
            throw new IllegalArgumentException("No security id provided for orderbook subscription!");
        }

        subscribeOrderBookMarketData(orderHandler.getClientOrder().getSecurityId(), orderHandler);

        subscribeMarketData(orderHandler);

        MicroStructure ms = MicroStructureDataManager.getInstance().getMicroStructure(orderHandler.getClientOrder().getSecurityId());
        if(ms==null){
            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.MARKET_DATA_SUBCRIBE_ERROR,
                            orderHandler.getClientOrder().getClientOrderId()),
                    "Neither "+orderHandler.getClientOrder().getSecurityId()+" nor default micro structure found!",null);
        }else{
            if(ms.getSymbol().contains("default")){
                Alert.fireAlert(Alert.Severity.Minor,String.format(Alert.MARKET_DATA_SUBCRIBE_ERROR,
                                orderHandler.getClientOrder().getClientOrderId()),
                        "No micro structure found for "+orderHandler.getClientOrder().getSecurityId()+", use default instead.",null);
            }

            orderHandler.getClientOrder().setAdv20(ms.getAdv20());
            orderHandler.getClientOrder().setMdv21(ms.getMdv21());
        }
    }

    private void subscribeOrderBookMarketData(String symbol,OrderHandler orderHandler){
        try{
            OrderbookDataManager.getInstance().subscribeOrderBook(symbol, false, orderHandler);
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major, String.format(Alert.MARKET_DATA_SUBCRIBE_ERROR, orderHandler.getClientOrder().getClientOrderId()),
                    ex.getMessage(), ex);
        }
    }

    private void subscribeMarketData(OrderHandler orderHandler){
        try{
            RealTimeDataManager.getInstance().subscribeRealTimeData(orderHandler, false);
            RealTimeDataManager.getInstance().subscribeIntervalData(orderHandler, false);
            RealTimeDataManager.getInstance().subscribeAlldayIntervalData(orderHandler, false);
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major, String.format(Alert.MARKET_DATA_SUBCRIBE_ERROR, orderHandler.getClientOrder().getClientOrderId()),
                    ex.getMessage(), ex);
        }
    }
}
