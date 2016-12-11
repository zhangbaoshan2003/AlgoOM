package com.csc108.disruptor.eventConsumer;

import com.csc108.disruptor.event.OmEvent;
import com.csc108.log.LogFactory;
import com.csc108.model.fixModel.order.OrderHandler;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;

/**
 * Created by zhangbaoshan on 2016/5/6.
 */
public class EventHandlerBase implements EventHandler<OmEvent> {

    protected final Logger logger = LogFactory.getEventLogger();


    /**
     * Handle the given event.
     * @param event the given event to handle.
     * @param sequence the sequence of this event in ring buffer.
     * @param endOfBatch end of batch
     */
    public void onEvent(OmEvent event, long sequence, boolean endOfBatch)
            throws Exception {
        OmEvent localEvent = event;
        //localEvent.setDataHandler(event.getDataHandler());
        try {
            EventHandlerBase handler = event.getEventType().getHandler();
            if (handler != null) {
                handler.handle(localEvent);
                //publishLog(handler.getHandlerName(),localEvent,handler.doingWhat(localEvent));
            } else {
                logger.error("Cannot find a handler for the incoming event. " + event);
            }
        } catch (Exception exception) {
            logger.error("There was an exception caught when handing event, " + event,
                    exception);
        }
    }

    /*actual process event logic implemented in sub class*/
    public void handle(OmEvent eventSource){

    }

    public String getHandlerName(){
        return "Base";
    }

    public String doingWhat(OmEvent eventSource){

        return "";
    }

    public void publishLog(String handlerName, OmEvent eventSource,String action){
        if(eventSource!=null && eventSource.getDataHandler()!=null){
            OrderHandler handler = (OrderHandler)eventSource.getDataHandler();
            if(handler!=null){
                logger.info(String.format("%s triggered to handle [%s] to perform %s",
                        handlerName,handler.getClientOrder().getClientOrderId(),action));
            }
        }
    }
}
