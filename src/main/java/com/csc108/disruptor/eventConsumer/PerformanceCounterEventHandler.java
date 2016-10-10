package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.PerformanceCounter;
import com.csc108.disruptor.event.OmEvent;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class PerformanceCounterEventHandler extends EventHandlerBase {
    public static PerformanceCounterEventHandler Instance= new PerformanceCounterEventHandler();

    private PerformanceCounterEventHandler(){

    }

    @Override
    public String getHandlerName(){
        return "PerformanceCounterEventHandler";
    }

    public void handle(OmEvent eventSource){
        PerformanceCounter.increase();
    }
}
