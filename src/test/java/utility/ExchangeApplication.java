package utility;

import com.csc108.log.LogFactory;
import com.csc108.model.AllocationCategory;
import com.csc108.model.AllocationDecisionType;
import com.csc108.model.fixModel.order.ExchangeOrder;
import com.csc108.model.fixModel.order.ManuallyOrder;
import quickfix.*;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.field.*;
import quickfix.fix42.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangbaoshan on 2015/12/25.
 */
public class ExchangeApplication  extends MessageCracker implements Application {

    public ConcurrentHashMap<String,ExchangeOrder> orderSet= new ConcurrentHashMap<>();

    private final CountDownLatch logonLatch;
    private DefaultMessageFactory messageFactory = new DefaultMessageFactory();
    private boolean isAvailable = true;
    private boolean isMissingField;
    private SessionID sessionID;

    public ConcurrentHashMap<String,ExchangeOrder> getOrderSet(){
        return orderSet;
    }

    static private HashMap<SessionID, HashSet<ExecID>> execIDs = new HashMap<SessionID, HashSet<ExecID>>();

    public ExchangeApplication(CountDownLatch logonLatch) {
        this.logonLatch = logonLatch;
    }

    public void onCreate(SessionID sessionID) {
        System.out.println(sessionID + " created!");
    }

    public void onLogon(SessionID sessionID) {
        System.out.println("check ..." + sessionID);
        this.sessionID = sessionID;
        this.logonLatch.countDown();
    }

    public void onLogout(SessionID sessionID) {

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
        crack(message, sessionID);
    }

    public void onMessage(OrderPauseResumeRequest message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {

    }

    public void onMessage(NewOrderSingle message,
                          SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, Exception,IncorrectTagValue {

        com.csc108.log.LogFactory.info("Exchange incoming :" + message.toString());

        double qtyOrder = message.get(new OrderQty()).getValue();
        double price = message.get(new Price()).getValue();
        ExchangeOrder exchangeOrder = new ExchangeOrder(null,sessionID,qtyOrder,price, AllocationDecisionType.Unknown, AllocationCategory.Unknown);
        exchangeOrder.setSymbol(message.get(new Symbol()).getValue());
        exchangeOrder.setOrderSide(message.getSide());
        exchangeOrder.setClientOrderId(message.getString(11));
        exchangeOrder.setOrderQty((long)qtyOrder);
        exchangeOrder.setCumQty(0);
        orderSet.putIfAbsent(exchangeOrder.getClientOrderId(), exchangeOrder);

        acknowledgeOrder(exchangeOrder);

        if(TestUtility.Purpose==TestPurpose.FULL_FILL){
            fillOrder(exchangeOrder.getClientOrderId(), 3000);
            fillOrder(exchangeOrder.getClientOrderId(), 2000);
            fillOrder(exchangeOrder.getClientOrderId(),1000);
            fillOrder(exchangeOrder.getClientOrderId(),4000);
        }else if(TestUtility.Purpose==TestPurpose.PARTIAL_FILL_THEN_CANCEL){
            fillOrder(exchangeOrder.getClientOrderId(),qtyOrder/2);
        }else if(TestUtility.Purpose==TestPurpose.CANCEL_THEN_PARTIAL_FILL){
        }else if(TestUtility.Purpose==TestPurpose.CANCEL_DIRECTLY_FROM_EXG){
            fillOrder(exchangeOrder.getClientOrderId(),qtyOrder/2);

            //response canceled
            ExecutionReport canceledReport = new ExecutionReport(
                    new OrderID(exchangeOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.CANCELED),
                    new OrdStatus(OrdStatus.CANCELED),
                    new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),
                    new LeavesQty(exchangeOrder.getLeavesQty()),
                    new CumQty(exchangeOrder.getCumQty()),
                    new AvgPx(exchangeOrder.getPrice())
            );
            canceledReport.set(new ClOrdID(UUID.randomUUID().toString()));
            canceledReport.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
            canceledReport.set(new LastPx(0));
            canceledReport.set(new LastShares(0));
            Session.sendToTarget(canceledReport, this.sessionID);

        }else if(TestUtility.Purpose==TestPurpose.MANUALLY_ORDER_CANCEL_THEN_PARTIAL_FILL){
            responseManuuallyOrderCancelThenPartialFill(exchangeOrder);

        }else if(TestUtility.Purpose==TestPurpose.MANUALLY_ORDER_FILLED_THEN_PARTIAL_FILL){
            responseManuuallyOrderFilledThenPartialFill(exchangeOrder);

        }else if(TestUtility.Purpose==TestPurpose.CANCEL_REJECT_THEN_FILL_DIRECTLY_FROM_EXG){
            OrderCancelReject reject = new OrderCancelReject(new OrderID( UUID.randomUUID().toString()), new ClOrdID(UUID.randomUUID().toString()),
                    new OrigClOrdID(exchangeOrder.getClientOrderId()),new OrdStatus(OrdStatus.FILLED),new CxlRejResponseTo('1'));
            Session.sendToTarget(reject,this.sessionID);

            fillOrder(exchangeOrder.getClientOrderId(),qtyOrder);
        }
    }

    private void fillOrder(String cloOrderId,double qtyToFill) throws FieldNotFound, SessionNotFound {
        ExchangeOrder exchangeOrder = orderSet.get(cloOrderId);
        if(exchangeOrder==null)
            throw new NullPointerException("Can't find exchange order for "+cloOrderId);

        exchangeOrder.setCumQty(exchangeOrder.getCumQty() + (long) qtyToFill);

        OrdStatus ordStatus = null;
        ExecType execType = null;
        if(exchangeOrder.getLeavesQty()==0){
            ordStatus = new OrdStatus(OrdStatus.FILLED);
            execType=new ExecType(ExecType.FILL);
        }else{
            ordStatus = new OrdStatus(OrdStatus.PARTIALLY_FILLED);
            execType=new ExecType(ExecType.PARTIAL_FILL);
        }

        //response fill response
        ExecutionReport fillReport = new ExecutionReport(
                new OrderID(cloOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                execType,//new ExecType(ExecType.FILL),
                //new OrdStatus(OrdStatus.FILLED),
                ordStatus,
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(exchangeOrder.getLeavesQty()),
                new CumQty(exchangeOrder.getCumQty()),
                new AvgPx(exchangeOrder.getPrice())
        );
        fillReport.set(new ClOrdID(exchangeOrder.getClientOrderId()));
        fillReport.set(new LastPx(exchangeOrder.getPrice()));
        fillReport.set(new LastShares(qtyToFill));
        Session.sendToTarget(fillReport, this.sessionID);
    }

    private void acknowledgeOrder(ExchangeOrder exchangeOrder) throws FieldNotFound, SessionNotFound {
        //response new ack
        String exchangeOrderId=exchangeOrder.getClientOrderId();
        ExecutionReport acknowledgement = new ExecutionReport(
                new OrderID(exchangeOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.NEW),
                new OrdStatus(OrdStatus.NEW),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(exchangeOrder.getOrderQty()),
                new CumQty(0),
                new AvgPx(exchangeOrder.getPrice())
        );
        acknowledgement.set(new ClOrdID(exchangeOrderId));
        Session.sendToTarget(acknowledgement, this.sessionID);
    }

    public void onMessage( OrderCancelRequest message,
                           SessionID sessionID )throws Exception {

        String originalOrderId = message.getString(41);
        ExchangeOrder exchangeOrder = orderSet.get(originalOrderId);

        if(TestUtility.Purpose==TestPurpose.PARTIAL_FILL_THEN_CANCEL){
            //response pending cancel
            ExecutionReport pendingCancelReport = new ExecutionReport(
                    new OrderID(exchangeOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.PENDING_CANCEL),
                    new OrdStatus(OrdStatus.PENDING_CANCEL),
                    new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),
                    new LeavesQty(exchangeOrder.getLeavesQty()),
                    new CumQty(exchangeOrder.getCumQty()),
                    new AvgPx(exchangeOrder.getPrice())
            );
            pendingCancelReport.set(new ClOrdID(message.getString(11)));
            pendingCancelReport.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
            pendingCancelReport.set(new LastPx(exchangeOrder.getPrice()));
            pendingCancelReport.set(new LastShares(exchangeOrder.getLeavesQty()));
            Session.sendToTarget(pendingCancelReport, this.sessionID);

            //response canceled
            ExecutionReport canceledReport = new ExecutionReport(
                    new OrderID(exchangeOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.CANCELED),
                    new OrdStatus(OrdStatus.CANCELED),
                    new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),
                    new LeavesQty(exchangeOrder.getLeavesQty()),
                    new CumQty(exchangeOrder.getCumQty()),
                    new AvgPx(exchangeOrder.getPrice())
            );
            canceledReport.set(new ClOrdID(message.getString(11)));
            canceledReport.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
            canceledReport.set(new LastPx(exchangeOrder.getPrice()));
            canceledReport.set(new LastShares(exchangeOrder.getLeavesQty()));
            Session.sendToTarget(canceledReport, this.sessionID);
        }

        if(TestUtility.Purpose==TestPurpose.CANCEL_REJECTED){
            try{
                OrderCancelReject cancelReject = new OrderCancelReject(
                        new OrderID(exchangeOrder.getClientOrderId()),
                        new ClOrdID(message.getClOrdID().getValue()),
                        new OrigClOrdID(exchangeOrder.getClientOrderId()),
                        new OrdStatus(OrdStatus.FILLED),
                        new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST)
                        );
                Session.sendToTarget(cancelReject, this.sessionID);
            }catch (Exception ex){
                LogFactory.error("Cancel reject error!",ex);
            }
        }

        if(TestUtility.Purpose==TestPurpose.CANCEL_THEN_PARTIAL_FILL){
            double orderQty = exchangeOrder.getOrderQty();

            ExecutionReport canceledReport = new ExecutionReport(
                    new OrderID(exchangeOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.CANCELED),
                    new OrdStatus(OrdStatus.CANCELED),
                    new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),
                    new LeavesQty(orderQty/2),
                    new CumQty(orderQty/2),
                    new AvgPx(exchangeOrder.getPrice())
            );
            canceledReport.set(new ClOrdID(message.getString(11)));
            canceledReport.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
            canceledReport.set(new LastPx(0));
            canceledReport.set(new LastShares(0));
            Session.sendToTarget(canceledReport, this.sessionID);

            //Thread.sleep(100);

            fillOrder(exchangeOrder.getClientOrderId(), orderQty / 2);
        }

        if(TestUtility.Purpose == TestPurpose.PEGGING_CANCEL){
            //response pending cancel
            exchangeOrder.setOrdStatus(new OrdStatus(OrdStatus.PENDING_CANCEL));

            ExecutionReport pendingCancelReport = new ExecutionReport(
                    new OrderID(exchangeOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.PENDING_CANCEL),
                    new OrdStatus(OrdStatus.PENDING_CANCEL),
                    new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),
                    new LeavesQty(exchangeOrder.getLeavesQty()),
                    new CumQty(exchangeOrder.getCumQty()),
                    new AvgPx(exchangeOrder.getPrice())
            );
            pendingCancelReport.set(new ClOrdID(message.getString(11)));
            pendingCancelReport.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
            pendingCancelReport.set(new LastPx(exchangeOrder.getPrice()));
            pendingCancelReport.set(new LastShares(exchangeOrder.getLeavesQty()));
            Session.sendToTarget(pendingCancelReport, this.sessionID);

            //response canceled
            ExecutionReport canceledReport = new ExecutionReport(
                    new OrderID(exchangeOrder.getClientOrderId()),
                    new ExecID(UUID.randomUUID().toString()),
                    new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.CANCELED),
                    new OrdStatus(OrdStatus.CANCELED),
                    new Symbol(exchangeOrder.getSymbol()),
                    exchangeOrder.getOrderSide(),
                    new LeavesQty(exchangeOrder.getLeavesQty()),
                    new CumQty(exchangeOrder.getCumQty()),
                    new AvgPx(exchangeOrder.getPrice())
            );
            canceledReport.set(new ClOrdID(message.getString(11)));
            canceledReport.set(new OrigClOrdID(exchangeOrder.getClientOrderId()));
            canceledReport.set(new LastPx(exchangeOrder.getPrice()));
            canceledReport.set(new LastShares(exchangeOrder.getLeavesQty()));
            exchangeOrder.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
            Session.sendToTarget(canceledReport, this.sessionID);
        }
    }

    private void responseManuuallyOrderFilledThenPartialFill(ExchangeOrder exchangeOrder) throws Exception {
        String exchangeOrderId=exchangeOrder.getClientOrderId();
        ExecutionReport acknowledgement = new ExecutionReport(
                new OrderID("Manually_"+exchangeOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.NEW),
                new OrdStatus(OrdStatus.NEW),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(exchangeOrder.getOrderQty()),
                new CumQty(0),
                new AvgPx(exchangeOrder.getPrice())
        );

        acknowledgement.setString(ManuallyOrder.TAG_MANUA_MASTER_ORDER_ID,"123456");
        acknowledgement.set(new ClOrdID("Manually_" + exchangeOrderId));
        acknowledgement.set(new Price(exchangeOrder.getPrice()));
        acknowledgement.set(new OrderQty(exchangeOrder.getOrderQty()));

        Session.sendToTarget(acknowledgement, this.sessionID);

        //fill
        double partialFillQty=100;
        double fillQty = exchangeOrder.getOrderQty()-partialFillQty;
        ExecutionReport fillReport = new ExecutionReport(
                new OrderID("Manually_" + exchangeOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.FILL),
                new OrdStatus(OrdStatus.FILLED),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(0),
                new CumQty(fillQty),
                new AvgPx(exchangeOrder.getPrice())
        );
        fillReport.set(new ClOrdID("Manually_" + exchangeOrder.getClientOrderId()));
        fillReport.set(new LastPx(exchangeOrder.getPrice()));
        fillReport.set(new LastShares((long)fillQty));
        exchangeOrder.setCumQty((long)fillQty);
        Session.sendToTarget(fillReport, this.sessionID);


//        //partialFilled
//        double leavesQty = exchangeOrder.getOrderQty()-partialFillQty;
//        ExecutionReport canceledReport = new ExecutionReport(
//                new OrderID("Manually_" +exchangeOrder.getClientOrderId()),
//                new ExecID(UUID.randomUUID().toString()),
//                new ExecTransType(ExecTransType.NEW),
//                new ExecType(ExecType.PARTIAL_FILL),
//                new OrdStatus(OrdStatus.PARTIALLY_FILLED),
//                new Symbol(exchangeOrder.getSymbol()),
//                exchangeOrder.getOrderSide(),
//                new LeavesQty(leavesQty),
//                new CumQty(partialFillQty),
//                new AvgPx(exchangeOrder.getPrice())
//        );
//        canceledReport.set(new ClOrdID(UUID.randomUUID().toString()));
//        canceledReport.set(new OrigClOrdID("Manually_" +exchangeOrder.getClientOrderId()));
//        canceledReport.set(new LastPx(0));
//        canceledReport.set(new LastShares(0));
//        Session.sendToTarget(canceledReport, this.sessionID);

    }

    private void responseManuuallyOrderCancelThenPartialFill(ExchangeOrder exchangeOrder) throws Exception {
        String exchangeOrderId=exchangeOrder.getClientOrderId();
        ExecutionReport acknowledgement = new ExecutionReport(
                new OrderID("Manually_"+exchangeOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.NEW),
                new OrdStatus(OrdStatus.NEW),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(exchangeOrder.getOrderQty()),
                new CumQty(0),
                new AvgPx(exchangeOrder.getPrice())
        );

        acknowledgement.setString(ManuallyOrder.TAG_MANUA_MASTER_ORDER_ID,"123456");
        acknowledgement.set(new ClOrdID("Manually_" + exchangeOrderId));
        acknowledgement.set(new Price(exchangeOrder.getPrice()));
        acknowledgement.set(new OrderQty(exchangeOrder.getOrderQty()));

        Session.sendToTarget(acknowledgement, this.sessionID);

        //partial fill
        double fillQty = exchangeOrder.getOrderQty()/4;
        ExecutionReport fillReport = new ExecutionReport(
                new OrderID("Manually_" + exchangeOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.PARTIAL_FILL),
                new OrdStatus(OrdStatus.PARTIALLY_FILLED),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(exchangeOrder.getOrderQty()-fillQty),
                new CumQty(fillQty),
                new AvgPx(exchangeOrder.getPrice())
        );
        fillReport.set(new ClOrdID("Manually_" + exchangeOrder.getClientOrderId()));
        fillReport.set(new LastPx(exchangeOrder.getPrice()));
        fillReport.set(new LastShares((long)fillQty));
        exchangeOrder.setCumQty((long)fillQty);
        Session.sendToTarget(fillReport, this.sessionID);


        //cancelled
        double leavsFinal = 1500;

        ExecutionReport canceledReport = new ExecutionReport(
                new OrderID("Manually_" +exchangeOrder.getClientOrderId()),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.CANCELED),
                new OrdStatus(OrdStatus.CANCELED),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(leavsFinal),
                new CumQty(exchangeOrder.getOrderQty()-leavsFinal),
                new AvgPx(exchangeOrder.getPrice())
        );
        canceledReport.set(new ClOrdID(UUID.randomUUID().toString()));
        canceledReport.set(new OrigClOrdID("Manually_" +exchangeOrder.getClientOrderId()));
        canceledReport.set(new LastPx(0));
        canceledReport.set(new LastShares(0));
        Session.sendToTarget(canceledReport, this.sessionID);


        //partial fill
        fillQty = exchangeOrder.getOrderQty()-fillQty-leavsFinal;
        fillReport = new ExecutionReport(
                new OrderID("Manually_" + exchangeOrderId),
                new ExecID(UUID.randomUUID().toString()),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(ExecType.PARTIAL_FILL),
                new OrdStatus(OrdStatus.PARTIALLY_FILLED),
                new Symbol(exchangeOrder.getSymbol()),
                exchangeOrder.getOrderSide(),
                new LeavesQty(leavsFinal),
                new CumQty(exchangeOrder.getOrderQty()-leavsFinal),
                new AvgPx(exchangeOrder.getPrice())
        );
        fillReport.set(new ClOrdID("Manually_" + exchangeOrder.getClientOrderId()));
        fillReport.set(new LastPx(exchangeOrder.getPrice()));
        fillReport.set(new LastShares(fillQty));
        exchangeOrder.setCumQty((long)fillQty);
        Session.sendToTarget(fillReport, this.sessionID);
    }
}