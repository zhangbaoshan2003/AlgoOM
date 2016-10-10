package model;

import com.csc108.disruptor.eventConsumer.EventHandlerBase;
import com.csc108.disruptor.eventConsumer.NewOrderEventHandler;
import junit.framework.TestCase;

/**
 * Created by LEGEN on 2016/7/24.
 */
public class EventHandlerTest extends TestCase {
    public void testHanderLog(){
        EventHandlerBase handler = NewOrderEventHandler.Instance;
        String handlerName = "NewOrderEventHandler";
        assertEquals(handlerName,handler.getHandlerName());
    }
}
