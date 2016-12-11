package com.csc108.model.fixModel.order;

import com.csc108.log.LogFactory;
import com.csc108.model.fixModel.sessionPool.SessionPool;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import quickfix.FieldNotFound;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.OrderCancelReject;
import quickfix.fix42.OrderCancelRequest;

import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/6/8.
 */
public class ManuallyOrder {
    public static final int TAG_MANUA_MASTER_ORDER_ID=11000;
    private final String manuallyClOrdID;
    private final String masterOrderConfirmationID;

    private double avgPrice = 0.0;
    private double orderQty=0.0;
    private double cumQty =0.0;
    private double leavesQty=0.0;
    private String symbol;
    private Side orderSide;

    private OrdStatus ordStatus = new OrdStatus(OrdStatus.NEW);

    public double getLeavsQty(){
        return leavesQty;
    }

    public double getOrderQty(){
        return orderQty;
    }

    public double getCumQty(){
        return cumQty;
    }

    public ManuallyOrder(ExecutionReport report) throws Exception {
        if(report.isSetField(11000)==false)
            throw new IllegalArgumentException("There is no tag11000 for PB manually order");
        manuallyClOrdID = report.getString(ClOrdID.FIELD);
        masterOrderConfirmationID = report.getString(TAG_MANUA_MASTER_ORDER_ID);

        //initialize
        try{
            OrderQty orderQtyField= new OrderQty();
            report.get(orderQtyField);
            orderQty = orderQtyField.getValue();
            cumQty=0.0;
            leavesQty = orderQty;
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical, String.format(Alert.FIELD_NOT_FOUND_KEY, "Order quantity", this.getManuallyClOrdID()),
                    report.toString(), ex);
        }

        try{
            Price price = new Price();
            report.get(price);
            avgPrice = price.getValue();

        }catch (FieldNotFound ex){

        }

        try{
            Symbol symbolField= new Symbol();
            report.get(symbolField);
            this.symbol = symbolField.getValue();

        }catch (FieldNotFound ex){

        }

        try{
            Side sideField= new Side();
            report.get(sideField);
            this.orderSide = sideField;

        }catch (FieldNotFound ex){

        }

    }

    public void processReportAndNotifyClient(ExecutionReport message) throws Exception {
        if(FixUtil.IsOrderCompleted(ordStatus)==true)
            throw new IllegalArgumentException("Manually order has been completed as "+ordStatus);

        ExecType execType = message.getExecType();
        //LogFactory.warn("Remove manually order text:"+message.getText());
        message.removeField(58);

        switch (execType.getValue()){
            case ExecType.NEW:
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually New)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.PENDING_NEW:
                //FixMsgHelper.sendMessage(message,sessionID,"Client << Exec Report(Manually Pending New)",this.manuallyClOrdID);
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Pending New)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.PARTIAL_FILL:
                double lastPx =message.getField(new LastPx()).getValue();
                double lastFillShares = message.getField(new LastShares()).getValue();
                avgPrice = (avgPrice*cumQty+lastPx*lastFillShares)/(cumQty+lastFillShares);
                cumQty = cumQty+lastFillShares;
                leavesQty = leavesQty-lastFillShares;
                ordStatus = new OrdStatus(OrdStatus.PARTIALLY_FILLED);

                //FixMsgHelper.sendMessage(message,sessionID,"Client << Exec Report(Manually Partial Fill)",this.manuallyClOrdID);
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Partial Fill)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.FILL:
                double lastPxFromFill =message.getField(new LastPx()).getValue();

                LeavesQty leavesQtyFieldFromFilledReport= new LeavesQty();
                message.get(leavesQtyFieldFromFilledReport);
                double lastShareFilled = leavesQtyFieldFromFilledReport.getValue();

                if((lastShareFilled+this.getCumQty())!=this.orderQty){
                    //missing a partial fill, suplement it
                    double missQty = this.orderQty - (lastShareFilled+this.getCumQty());
                    cumQty = cumQty+missQty;

                    ExecutionReport fillReport = new ExecutionReport(
                            new OrderID(manuallyClOrdID),
                            new ExecID(UUID.randomUUID().toString()),
                            new ExecTransType(ExecTransType.NEW),
                            new ExecType(ExecType.PARTIAL_FILL),
                            new OrdStatus(OrdStatus.PARTIALLY_FILLED),
                            new Symbol(this.symbol),
                            this.orderSide,
                            new LeavesQty(),
                            new CumQty(this.getCumQty()+missQty),
                            new AvgPx(this.avgPrice)
                    );
                    fillReport.set(new ClOrdID(manuallyClOrdID));
                    fillReport.set(new LastPx(lastPxFromFill));
                    fillReport.set(new LastShares(missQty));

                    //FixMsgHelper.sendMessage(fillReport, sessionID, "Client << Exec Report(Manually Partial Fill due to wrong cancel qty)", this.manuallyClOrdID);
                    SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                        try {
                            FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Partial Fill due to wrong fill qty)", this.manuallyClOrdID);
                        } catch (Exception ex) {
                            LogFactory.error("Iterate send out manually order response failed!", ex);
                        }
                    });
                }

                ordStatus = new OrdStatus(OrdStatus.FILLED);
                //FixMsgHelper.sendMessage(message,sessionID,"Client << Exec Report(Manually Fill)",this.manuallyClOrdID);
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Fill)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.PENDING_CANCEL:
                ordStatus = new OrdStatus(OrdStatus.PENDING_CANCEL);
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Pending Cancel)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.EXPIRED:
                ordStatus = new OrdStatus(OrdStatus.EXPIRED);
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Expired)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.CANCELED:
                CumQty cumQtyField = new CumQty();
                message.get(cumQtyField);

                LeavesQty leavesQtyField= new LeavesQty();
                message.get(leavesQtyField);

                if(cumQtyField.getValue()!=this.cumQty){
                    double shouldFill = cumQtyField.getValue() - cumQty;

                    //wrong order, supplement a partial fill
                    ExecutionReport fillReport = new ExecutionReport(
                            new OrderID(manuallyClOrdID),
                            new ExecID(UUID.randomUUID().toString()),
                            new ExecTransType(ExecTransType.NEW),
                            new ExecType(ExecType.PARTIAL_FILL),
                            new OrdStatus(OrdStatus.PARTIALLY_FILLED),
                            new Symbol(this.symbol),
                            this.orderSide,
                            new LeavesQty(),
                            new CumQty(cumQtyField.getValue()),
                            new AvgPx(this.avgPrice)
                    );
                    fillReport.set(new ClOrdID(manuallyClOrdID));
                    fillReport.set(new LastPx(avgPrice));
                    fillReport.set(new LastShares(shouldFill));

                    //FixMsgHelper.sendMessage(fillReport, sessionID, "Client << Exec Report(Manually Partial Fill due to wrong cancel qty)", this.manuallyClOrdID);
                    SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                        try {
                            FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Partial Fill due to wrong cancel qty)", this.manuallyClOrdID);
                        } catch (Exception ex) {
                            LogFactory.error("Iterate send out manually order response failed!", ex);
                        }
                    });
                }

                cumQty = cumQtyField.getValue();
                leavesQty =leavesQtyField.getValue();

                ordStatus = new OrdStatus(OrdStatus.CANCELED);
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Canceled)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;

            case ExecType.REJECTED:
                SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
                    try {
                        FixMsgHelper.sendMessage(message, sessionID, "Client << Exec Report(Manually Rejected)", this.manuallyClOrdID);
                    } catch (Exception ex) {
                        LogFactory.error("Iterate send out manually order response failed!", ex);
                    }
                });
                break;
        }
    }

    public void notifyClient(OrderCancelReject reject) throws Exception {
        LogFactory.warn("Cancel manually order rejected @ " + reject);
        reject.removeField(58);
        SessionPool.getInstance().getClientSessions().forEach(sessionID -> {
            try {
                FixMsgHelper.sendMessage(reject, sessionID, "Client << Order Cancel Rejected(Manually Order)", this.manuallyClOrdID);
            } catch (Exception ex) {
                LogFactory.error("Iterate send out manually order cancel rejected failed!", ex);
            }
        });
    }

    public void notifyClient(OrderCancelRequest cancelRequest) throws Exception {
        SessionPool.getInstance().getAlgoExchangeSessions().forEach(sessionID -> {
            try {
                FixMsgHelper.sendMessage(cancelRequest, sessionID, "EXG << Order Cancel Request(Manually Order)", this.manuallyClOrdID);
            } catch (Exception ex) {
                LogFactory.error("Iterate send out manually order cancel request failed!", ex);
            }
        });
    }

    public String getManuallyClOrdID() {
        return manuallyClOrdID;
    }


    public OrdStatus getOrdStatus() {
        return ordStatus;
    }
}
