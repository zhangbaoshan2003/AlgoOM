package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.event.OmEvent;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.market.AllDayIntervalMarketData;
import com.csc108.model.market.IntervalMarketData;
import com.csc108.model.market.RealTimeMarketData;
import com.csc108.utility.Alert;
import com.csc108.utility.FixUtil;

/**
 * Created by zhangbaoshan on 2016/8/17.
 */
public class MarketDataUpdatedEventHandler extends EventHandlerBase {
    public static MarketDataUpdatedEventHandler Instance = new MarketDataUpdatedEventHandler();

    private MarketDataUpdatedEventHandler() {
    }

    @Override
    public String getHandlerName() {
        return "MarketDataUpdatedEventHandler";
    }

    public void handle(OmEvent eventSource) {
        OrderHandler orderHandler = (OrderHandler) eventSource.getDataHandler();

        if(FixUtil.IsOrderCompleted(orderHandler.getClientOrder().getOrdStatus())==true)
            return;

        try {
            if (eventSource.getTriggerData() != null) {
                if (eventSource.getTriggerData() instanceof RealTimeMarketData) {
                    RealTimeMarketData eventData = (RealTimeMarketData) eventSource.getTriggerData();
                    orderHandler.setRealTimeMarketData(eventData);
                    orderHandler.process();
                }

                if (eventSource.getTriggerData() instanceof IntervalMarketData) {
                    IntervalMarketData eventData = (IntervalMarketData) eventSource.getTriggerData();
                    orderHandler.setIntervalMarketData(eventData);
                    orderHandler.process();
                }

                if (eventSource.getTriggerData() instanceof AllDayIntervalMarketData) {
                    AllDayIntervalMarketData eventData = (AllDayIntervalMarketData) eventSource.getTriggerData();
                    orderHandler.setAllDayIntervalMarketData(eventData);
                    orderHandler.process();
                }
            }
        } catch (Exception ex) {
            Alert.fireAlert(Alert.Severity.Major, String.format(Alert.MARKET_DATA_UPDATE_ERROR, orderHandler),
                    ex.getMessage(), ex);
        }
    }
}
