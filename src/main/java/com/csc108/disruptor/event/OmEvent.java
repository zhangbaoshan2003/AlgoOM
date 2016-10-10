package com.csc108.disruptor.event;

import com.csc108.model.IDataHandler;
import com.csc108.model.IEvaluationData;

/**
 * Created by zhangbaoshan on 2016/5/6.
 * This is the event holding will be put to ring buffer, including fix message event and market
 * data update triggered event
 */
public class OmEvent {
    //timestamp when it is in queue
    private long enqueueTimeStamp;

    //what type of this event is
    private EventType eventType;

    /*data wrapper and manager for a fix related or market data updated related event*/
    private IDataHandler dataManager;

    //link to an fix message or a market data
    private IEvaluationData triggerData;

    public IDataHandler getDataHandler() {
        return dataManager;
    }

    public void setDataHandler(IDataHandler dataManager) {
        this.dataManager = dataManager;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public long getEnqueueTimeStamp() {
        return enqueueTimeStamp;
    }

    public void setEnqueueTimeStamp(long enqueueTimeStamp) {
        this.enqueueTimeStamp = enqueueTimeStamp;
    }

    public IEvaluationData getTriggerData() {
        return triggerData;
    }

    public void setTriggerData(IEvaluationData triggerData) {
        this.triggerData = triggerData;
    }
}
