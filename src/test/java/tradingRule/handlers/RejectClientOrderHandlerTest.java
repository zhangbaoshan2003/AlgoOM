package tradingRule.handlers;

import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.tradingRule.core.IHandler;
import com.csc108.tradingRule.handlers.RejectClientOrderHandler;
import junit.framework.TestCase;
import quickfix.SessionID;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import utility.TestFixMsgHelper;

import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * Created by zhangbaoshan on 2016/11/1.
 */
public class RejectClientOrderHandlerTest extends TestCase {

    public void testHandlerCalled() throws Exception {
        IHandler handler = new RejectClientOrderHandler();

        handler = mock(RejectClientOrderHandler.class);
        handler.handle(null, null);
        verify(handler, times(1)).handle(null, null);

        boolean expectedException=false;

        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("IBM",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                1000,10.2);

        SessionID sessionID = mock(SessionID.class);
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,sessionID);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);

        handler = new RejectClientOrderHandler();

        clientOrder.setOrdStatus(new OrdStatus(OrdStatus.PENDING_CANCEL));
        try{
            handler.handle(orderHandler,new LinkedHashMap<>());
        }catch (IllegalArgumentException ex){
            expectedException=true;
            System.out.println(ex.getMessage());
        }
        assertEquals(true,expectedException);

        clientOrder.setOrdStatus(new OrdStatus(OrdStatus.FILLED));
        try{
            handler.handle(orderHandler,new LinkedHashMap<>());
        }catch (IllegalArgumentException ex){
            expectedException=true;
            System.out.println(ex.getMessage());
        }
        assertEquals(true,expectedException);

        expectedException=false;
        clientOrder = new ClientOrder(newOrderSingle,sessionID);
        orderHandler= new OrderHandler(clientOrder,null);
        try{
            handler.handle(orderHandler,new LinkedHashMap<>());
        }catch (IllegalArgumentException ex){
            expectedException=true;
            System.out.println(ex.getMessage());
        }
        assertEquals(false,expectedException);
    }
}
