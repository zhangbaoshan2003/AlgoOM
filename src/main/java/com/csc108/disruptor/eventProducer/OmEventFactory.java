package com.csc108.disruptor.eventProducer;

import com.csc108.disruptor.event.OmEvent;

/**
 * Created by zhangbaoshan on 2016/5/6.
 */
public class OmEventFactory implements com.lmax.disruptor.EventFactory<OmEvent> {

    @Override
    public OmEvent newInstance() {
        return new OmEvent();
    }
}
