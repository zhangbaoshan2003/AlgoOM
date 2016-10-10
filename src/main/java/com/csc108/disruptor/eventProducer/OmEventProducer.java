package com.csc108.disruptor.eventProducer;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.IDataHandler;
import com.csc108.model.IEvaluationData;
import com.csc108.utility.Alert;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.RingBuffer;
import quickfix.SystemTime;

/**
 * Created by zhangbaoshan on 2016/5/6.
 */
public class OmEventProducer {
    // Right buffer for calc event.
    private final RingBuffer<OmEvent> ringBuffer;

    private static final EventTranslatorThreeArg<OmEvent,EventType, IDataHandler,IEvaluationData> TRANSLATOR
            =new EventTranslatorThreeArg<OmEvent,EventType,IDataHandler,IEvaluationData>(){
        public void translateTo(OmEvent event,long sequence,EventType eventType,
                                IDataHandler dataManager,IEvaluationData triggerData){
            event.setEventType(eventType);
            event.setDataHandler(dataManager);
            event.setTriggerData(triggerData);
        }
    };

    public OmEventProducer(RingBuffer<OmEvent> ringBuffer){
        this.ringBuffer = ringBuffer;
    }


    /**
     * Put an event to Ring buffer.
     * @param eventType event type of the given event.
     * @param dataHandler the given event.
     *
     */
    public void enqueueEvent(EventType eventType,IDataHandler dataHandler,IEvaluationData triggerData){
        if(dataHandler!=null){
            dataHandler.setEnqueueTimeStamp(SystemTime.currentTimeMillis());
        }

//        long eventsInquueue =ringBuffer.getCursor() % GlobalConfig.getBufferSize();
//        if(eventsInquueue+10>GlobalConfig.getBufferSize()){
//            Alert.fireAlert(Alert.Severity.Critical,"Too many events in ring bugger!@"+ringBuffer.getCursor(),
//                    Long.toString(eventsInquueue) ,null);
//        }
        ringBuffer.publishEvent(TRANSLATOR, eventType, dataHandler, triggerData);
    }
}
