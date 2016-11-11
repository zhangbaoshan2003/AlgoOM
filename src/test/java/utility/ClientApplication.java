package utility;

import com.csc108.decision.configuration.PegConfiguration;
import com.csc108.decision.pegging.PeggingDecision;
import com.csc108.log.LogFactory;
import com.csc108.model.criteria.Condition;
import com.csc108.model.fix.order.ClientOrder;
import com.csc108.model.fix.order.OrderHandler;
import quickfix.*;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.field.converter.DoubleConverter;
import quickfix.fix42.*;
import sun.rmi.runtime.*;
import sun.rmi.runtime.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangbaoshan on 2015/12/21.
 */
public class ClientApplication  extends MessageCracker implements  Application {

    private DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private boolean isAvailable = true;
    private boolean isMissingField;
    private SessionID sessionID;
    private final CountDownLatch logonLatch;

    static private HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<SessionID, HashSet<ExecID>>();

    private final ConcurrentHashMap<String,ClientOrder> orderSet;
    public ConcurrentHashMap<String,ClientOrder> getOrderSet(){
        return orderSet;
    }

    private final ConcurrentHashMap<String,CountDownLatch> gatewaysLocks = new ConcurrentHashMap<>();

    public SessionID getSessionID(){
        return sessionID;
    }

    public ClientApplication(CountDownLatch logonLatch,ConcurrentHashMap<String,ClientOrder> orderSet) {
        this.orderSet = orderSet;
        this.logonLatch = logonLatch;
    }

    public void onCreate(SessionID sessionID) {
    }

    public void onLogon(SessionID sessionID) {
        this.sessionID = sessionID;
        this.logonLatch.countDown();
    }

    public void onLogout(SessionID sessionID) {
        this.sessionID=null;
    }

    public void toAdmin(Message message, SessionID sessionID) {
    }

    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try{
            crack(message, sessionID);
        }catch (Exception ex){
            LogFactory.error("Crack message from client application error!",ex);
        }

    }

    public void onMessage(OrderPauseResumeRequest message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {

    }

    public void onMessage(NewOrderSingle message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {
    }

    public void onMessage(OrderCancelReject message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {

        String clientOrderId = message.getString(41);
        CountDownLatch gateWay = gatewaysLocks.get(clientOrderId);
        ClientOrder clientOrder = orderSet.get(clientOrderId);
        gateWay.countDown();

    }

    public void onMessage(ExecutionReport message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {

        if(TestUtility.Purpose==TestPurpose.CANCEL_REJECTED)
            return;

        double lastFill=0;
        ExecType execType = ((ExecutionReport)message).getExecType();

        String clientOrderId = "";
        CountDownLatch gateWay = null;
        ClientOrder clientOrder = null;

        switch (execType.getValue()){
            case ExecType.PENDING_NEW:
                clientOrderId = message.getString(11);
                gateWay = gatewaysLocks.get(clientOrderId);
                clientOrder = orderSet.get(clientOrderId);

                clientOrder.setOrdStatus(new OrdStatus(OrdStatus.PENDING_NEW));
                LogFactory.info("Receiving  pending new :" + clientOrderId);
                if(gateWay!=null){
                    gateWay.countDown();
                }
                break;

            case ExecType.NEW:
                clientOrderId = message.getString(11);
                gateWay = gatewaysLocks.get(clientOrderId);
                clientOrder = orderSet.get(clientOrderId);

                if(clientOrder!=null){
                    clientOrder.setOrdStatus(new OrdStatus(OrdStatus.NEW));
                    LogFactory.info("Receiving new :" + clientOrderId);
                }

                if(gateWay!=null){
                    gateWay.countDown();
                }

                break;

            case ExecType.PARTIAL_FILL:

                clientOrderId = message.getString(11);
                gateWay = gatewaysLocks.get(clientOrderId);
                clientOrder = orderSet.get(clientOrderId);

                if(clientOrder!=null){
                    clientOrder.setOrdStatus(new OrdStatus(OrdStatus.PARTIALLY_FILLED));
                    lastFill = message.get(new LastShares()).getValue();
                    clientOrder.setCumQty(clientOrder.getCumQty() + (long)lastFill);
                    LogFactory.info("Receiving  partial fill :" + clientOrderId);
                }


                if(gateWay!=null){
                    gateWay.countDown();
                }
                break;

            case ExecType.FILL:

                clientOrderId = message.getString(11);
                gateWay = gatewaysLocks.get(clientOrderId);
                clientOrder = orderSet.get(clientOrderId);
                if(clientOrder!=null){
                    clientOrder.setOrdStatus(new OrdStatus(OrdStatus.FILLED));
                    lastFill = message.get(new LastShares()).getValue();
                    clientOrder.setCumQty(clientOrder.getCumQty() + (long)lastFill);
                    LogFactory.info("Receiving  fill :" + clientOrderId + " leave qty " + clientOrder.getLeavesQty());
                }

                if(gateWay!=null){
                    gateWay.countDown();
                }
                break;

            case ExecType.PENDING_CANCEL:
                clientOrderId = message.getString(41);
                clientOrder = orderSet.get(clientOrderId);
                gateWay = gatewaysLocks.get(clientOrderId);
                clientOrder.setOrdStatus(new OrdStatus(OrdStatus.PENDING_CANCEL));
                if(gateWay!=null){
                    gateWay.countDown();
                }
                LogFactory.info("Receiving pending cancel  :" + clientOrderId);
                break;

            case ExecType.CANCELED:
                clientOrderId = message.getString(41);
                gateWay = gatewaysLocks.get(clientOrderId);
                if(gateWay==null){
                    gateWay = gatewaysLocks.get("Manually_"+clientOrderId);
                }

                clientOrder = orderSet.get(clientOrderId);

                if(clientOrder!=null){
                    clientOrder.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
                    LogFactory.info("Receiving canceled :" + clientOrderId);
                }



                if(gateWay!=null){
                    gateWay.countDown();
                }
                break;

            case ExecType.REJECTED:
                clientOrderId = message.getString(11);
                gateWay = gatewaysLocks.get(clientOrderId);
                clientOrder = orderSet.get(clientOrderId);

                clientOrder.setOrdStatus(new OrdStatus(OrdStatus.REJECTED));
                LogFactory.info("Receiving rejected response:" + clientOrderId);
                if(gateWay!=null){
                    gateWay.countDown();
                }
                break;
        }
    }


    public ClientOrder sendNewSingleOrderThenFilledIt(String symbol,double qty,double price,CountDownLatch gateWay) throws Exception {
        //todo: use correct hasp map to hold gate way
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg(symbol,new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                qty,price);
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,sessionID);
        OrderHandler manager = new OrderHandler(clientOrder,null);
        manager.initialize();

        orderSet.putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
        gatewaysLocks.putIfAbsent(clientOrder.getClientOrderId(),gateWay);
        Session.sendToTarget(newOrderSingle,this.sessionID);
        return clientOrder;
    }


    public ClientOrder sendNewSingleOrder(String symbol,double qty,double price,CountDownLatch gateWay) throws Exception {

        //todo: use correct hasp map to hold gate way
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg(symbol,new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                qty,price);
        ClientOrder clientOrder = new ClientOrder(newOrderSingle,sessionID);
        OrderHandler manager = new OrderHandler(clientOrder,null);
        manager.initialize();
        orderSet.putIfAbsent(clientOrder.getClientOrderId(), clientOrder);

        if(gateWay!=null){
            gatewaysLocks.putIfAbsent(clientOrder.getClientOrderId(),gateWay);
        }

        Session.sendToTarget(newOrderSingle,this.sessionID);
        return clientOrder;
    }

    public ClientOrder sendNewPeggingSingleOrder(String symbol,double qty,double price) throws Exception {
        //todo: use correct hasp map to hold gate way
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg(symbol,new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                qty,price);

        newOrderSingle.setString(PegConfiguration.PegDisplaySizeField,"1000");

        ClientOrder clientOrder = new ClientOrder(newOrderSingle,sessionID);
        OrderHandler manager = new OrderHandler(clientOrder,null);
        manager.initialize();
        orderSet.putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
        Session.sendToTarget(newOrderSingle,this.sessionID);
        return clientOrder;
    }

    public void cancelClientOrderWithoutSentOutExchangeOrders(String symbol,double qty,double price,CountDownLatch gateWay) throws Exception {
        ClientOrder clientOrder = sendNewSingleOrderThenFilledIt(symbol, qty, price, gateWay);
        OrderCancelRequest cancelRequest = new OrderCancelRequest(
                new OrigClOrdID(clientOrder.getClientOrderId()),
                new ClOrdID(UUID.randomUUID().toString()), new Symbol(clientOrder.getSymbol()),
                 clientOrder.getOrderSide(),new TransactTime());
        clientOrder.setCancelRequestMsg(cancelRequest);
        Session.sendToTarget(cancelRequest,this.sessionID);
    }

    public void cancelOrder(ClientOrder clientOrder) throws Exception {
        OrderCancelRequest cancelRequest = new OrderCancelRequest(
                new OrigClOrdID(clientOrder.getClientOrderId()),
                new ClOrdID(UUID.randomUUID().toString()), new Symbol(clientOrder.getSymbol()),
                clientOrder.getOrderSide(),new TransactTime());
        clientOrder.setCancelRequestMsg(cancelRequest);
        Session.sendToTarget(cancelRequest,this.sessionID);
    }

    public ClientOrder sendNewConditionalOrder(String symbol,double qty,double price,CountDownLatch gateWay) throws Exception {
        NewOrderSingle newOrderSingle =  TestFixMsgHelper.Instance.buildNewOrderSingleMsg(symbol,new Side(Side.BUY),new OrdType(OrdType.LIMIT),
                qty,price);
        newOrderSingle.setDouble(Condition.TAG_ABS_VALUE,3000);
        newOrderSingle.setInt(Condition.TAG_ACTION, 1);
        newOrderSingle.setInt(Condition.TAG_BENCHMARK,10);
        newOrderSingle.setInt(Condition.TAG_OP,0);
        newOrderSingle.setString(Condition.TAG_REFER_SECURITY, ".CSI300");

        ClientOrder clientOrder = new ClientOrder(newOrderSingle,sessionID);
        OrderHandler handler = new OrderHandler(clientOrder,null);
        handler.initialize();

        orderSet.putIfAbsent(clientOrder.getClientOrderId(),clientOrder);
        if(gateWay!=null){
            gatewaysLocks.putIfAbsent(clientOrder.getClientOrderId(), gateWay);
        }

        Session.sendToTarget(newOrderSingle,this.sessionID);
        return clientOrder;
    }
}