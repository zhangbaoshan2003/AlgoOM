package com.csc108.disruptor.eventConsumer;

import com.csc108.configuration.GlobalConfig;
import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.EventType;
import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/6/2.
 */
public class WakeupEventHandler extends EventHandlerBase {
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public static WakeupEventHandler Instance= new WakeupEventHandler();
    private WakeupEventHandler(){

    }

    @Override
    public String getHandlerName(){
        return "WakeupEventHandler";
    }

    @Override
    public void handle(OmEvent eventSource){

        if(eventSource.getDataHandler().getOriginalThreadId()!= Thread.currentThread().getId()){
            LogFactory.error("Evaluation event dispatch thread error", null);
            //return;
        }

        OrderHandler clientOrderHandler = (OrderHandler)eventSource.getDataHandler();
        ClientOrder clientOrder = clientOrderHandler.getClientOrder();
        String clientOrderId = clientOrder.getClientOrderId();
        DisruptorController controller =clientOrderHandler.getController();
        //EventDispatcher.getInstance().getControllerMap().get(clientOrderId);

        logger.info(String.format("Trigger wakeup handle event %s @ thread %d for order %s", eventSource.getDataHandler().getID(),
                Thread.currentThread().getId(), clientOrder.getClientOrderId()));

        //Todo: should involve logic of avoiding too often wakeup
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                controller.enqueueEvent(EventType.EVALUATION,clientOrderHandler,null);
            }
        },GlobalConfig.getWakeupInterval(), TimeUnit.SECONDS);
    }
}
