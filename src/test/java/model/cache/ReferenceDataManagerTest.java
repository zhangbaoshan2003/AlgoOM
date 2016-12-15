package model.cache;

import com.csc108.model.cache.ReferenceDataManager;
import com.csc108.model.fixModel.order.ClientOrder;
import com.csc108.model.fixModel.order.OrderHandler;
import junit.framework.TestCase;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix42.NewOrderSingle;
import utility.TestCaseBase;
import utility.TestFixMsgHelper;
import utility.TestUtility;

import java.time.LocalDateTime;

/**
 * Created by zhangbaoshan on 2016/12/12.
 */
public class ReferenceDataManagerTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        ReferenceDataManager.getInstance().init();

    }

    public void testNormalTradableOrder() throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-09:30:00","20161212-11:30:00");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        //normal order time
        boolean expectError=false;
        try {
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
        }catch (Exception ex){
            System.err.println(ex);
            expectError=true;
        }
        assertEquals(false, expectError);
    }

    public void testEffectiveTimeNotInTradiable() throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-09:29:00","20161212-11:30:00");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        //normal order time
        boolean expectError=false;
        try {
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
        }catch (Exception ex){
            System.err.println(ex);
            expectError=true;
        }
        assertEquals(true,expectError);
    }

    public void testExpireTimeNotInTradiable() throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-09:30:00","20161212-11:30:01");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        //normal order time
        boolean expectError=false;
        try {
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
        }catch (Exception ex){
            System.err.println(ex);
            expectError=true;
        }
        assertEquals(true,expectError);
    }

    public void testEffectiveExpireTimeInDifferentTradingSession() throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-09:30:00","20161212-15:00:00");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        //normal order time
        boolean expectError=false;
        try {
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
        }catch (Exception ex){
            System.err.println(ex);
            expectError=true;
        }
        assertEquals(false,expectError);
    }

    public void testEffectiveExpireTimeNotInDifferentTradingSession() throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-12:30:00","20161212-15:00:00");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        //normal order time
        boolean expectError=false;
        try {
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
        }catch (Exception ex){
            System.err.println(ex);
            expectError=true;
        }
        assertEquals(true,expectError);
    }

    public void testEffectiveExpireTimeNotLongerThan5Seconds() throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg("600000",new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                10000,12.5,"20161212-13:00:00","20161212-13:00:04");
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,null);
        OrderHandler orderHandler = new OrderHandler(clientOrder,null);
        orderHandler.initialize();

        //normal order time
        boolean expectError=false;
        try {
            ReferenceDataManager.getInstance().validateNewOrder(orderHandler);
        }catch (Exception ex){
            System.err.println(ex);
            expectError=true;
        }
        assertEquals(true,expectError);
    }
}
