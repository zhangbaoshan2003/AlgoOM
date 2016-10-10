package com.csc108.disruptor.concurrent;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.disruptor.eventConsumer.EventHandlerBase;
import com.csc108.disruptor.eventProducer.OmEventFactory;
import com.csc108.disruptor.eventProducer.OmEventProducer;
import com.csc108.log.LogFactory;
import com.csc108.model.IDataHandler;
import com.csc108.model.IEvaluationData;
import com.csc108.model.market.OrderBook;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import quickfix.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/5/7.
 */
public class DisruptorController implements ILifetimeCycle {

    /*Identifier the disrupor*/
    private int ID;

    public int getID(){
        return this.ID;
    }

    //factor for the event
    private final EventFactory<OmEvent> eventFactory = new OmEventFactory();

    //Event producer used to populate into event ring buffer
    private final OmEventProducer eventProducer;

    //disruptor
    private final Disruptor<OmEvent> disruptor;

    private final EventHandlerBase handlerBase = new EventHandlerBase();

    //ID will be used to identify this disruptor and its ring buffer
    public DisruptorController(int id){
        this.ID = id;
        disruptor = new Disruptor<OmEvent>(eventFactory, GlobalConfig.getBufferSize(),
                Executors.newSingleThreadExecutor(),
                ProducerType.MULTI,new SleepingWaitStrategy());

        disruptor.handleEventsWith(handlerBase);

        eventProducer = new OmEventProducer(disruptor.getRingBuffer());

    }

    @Override
    public void start(){
        try {
            disruptor.start();
            LogFactory.info(String.format("Disruptor %d started.",ID));
        }catch (Exception ex){
            LogFactory.error("Start disruptor error!",ex);
        }
    }

    @Override
    public void stop(){
        try {
            disruptor.shutdown(10, TimeUnit.SECONDS);
            LogFactory.info(String.format("Disruptor %d stopped.",ID));
        }catch (Exception ex){
            LogFactory.error("Start disruptor error!",ex);
        }
    }

    //enqueue event to ring buffer
    public void enqueueEvent(EventType eventType, IDataHandler orderHandler,IEvaluationData triggerData){
        eventProducer.enqueueEvent(eventType,orderHandler,triggerData);
    }
}
