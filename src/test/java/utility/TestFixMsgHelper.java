package utility;

import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/5/3.
 */
public class TestFixMsgHelper {

    public static TestFixMsgHelper Instance = new TestFixMsgHelper();

    private ArrayList<NewOrderSingle> newOrderSingleRequestPool = new ArrayList<>();
    private ArrayList<OrderCancelRequest> cancelRequestPool= new ArrayList<>();

    private TestFixMsgHelper(){

    }

    public synchronized NewOrderSingle buildNewOrderSingleMsg(String symbol,Side side,OrdType type,double qty,double price){
        NewOrderSingle newOrderSingle = new quickfix.fix42.NewOrderSingle(
                new ClOrdID(UUID.randomUUID().toString()), new HandlInst('1'),
                new Symbol(symbol),side,
                new TransactTime(new Date(0)),  type);

        //OrderID orderID = new OrderID("OrdID_" +UUID.randomUUID().toString());
        //newOrderSingle.setField(orderID);
        newOrderSingle.set(new OrderQty(qty));
        newOrderSingle.set(new Price(price));
        newOrderSingle.set(new SecurityExchange("SS"));
        newOrderSingle.set(new SecondaryClOrdID (UUID.randomUUID().toString()));
        newOrderSingle.set(new Account("ZBS"));
        newOrderSingleRequestPool.add(newOrderSingle);
        return newOrderSingle;
    }

    public synchronized NewOrderSingle buildNewOrderSingleMsg(String symbol,Side side,OrdType type,double qty,double price,String effectiveTime,String expireTime){
        NewOrderSingle newOrderSingle = buildNewOrderSingleMsg(symbol,side,type,qty,price);

        newOrderSingle.setString(6062,effectiveTime);
        newOrderSingle.setString(6063,expireTime);
        return newOrderSingle;
    }

    public synchronized OrderCancelRequest buildCancelRequestMsg(String orderIdToCancel){
        OrderCancelRequest cancelRequest = new OrderCancelRequest();
        cancelRequest.set(new OrigClOrdID(orderIdToCancel));
        cancelRequestPool.add(cancelRequest);
        return cancelRequest;
    }

    public ArrayList<NewOrderSingle> getNewOrderSingleRequestPool(){
        return this.newOrderSingleRequestPool;
    }
}
