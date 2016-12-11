package com.csc108.model.fixModel.order;

import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.log.LogFactory;
import com.csc108.model.AllocationCategory;
import com.csc108.model.AllocationDecisionType;
import com.csc108.model.OrderState;
import com.csc108.model.cache.TradeDataMqManager;
import com.csc108.utility.Alert;
import com.csc108.utility.FixMsgHelper;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/4/28.
 */
public class ExchangeOrder extends ClientOrder {
    private AllocationCategory allocationCategory = AllocationCategory.Unknown;
    private AllocationDecisionType decisionType = AllocationDecisionType.Unknown;

    private double avgPrice=0.0;

    //master-child relationship
    private ClientOrder parent;

    private SessionID sessionID;

    public void reportProgress() throws Exception {
//        if(this.getParent().getOrderHandler().isPeggingOrder()==false){
//            return;
//        }




        HashMap<String, String> data = new HashMap<>();
        data.put("OrderId", this.getClientOrderId());
        data.put("Price", Double.toString(getPrice()));
        data.put("Qty", Long.toString(getOrderQty()));
        data.put("CumQty", Long.toString(getCumQty()));
        data.put("LeavesQty", Double.toString(getLeavesQty()));
        data.put("AvgPrice", Double.toString(getAvgPrice()));
        data.put("OrderStatus", getFixStatusDisplay());
        data.put("TimeInQueue", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")));
        data.put("ClosedTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")));
        data.put("Category", getAllocationCategory().toString());
        data.put("DecisionType", getDecisionType().toString());
        data.put("Text", "Pegging");
        data.put("__HIDDEN_FIELDS__", "RootClOrdId,RCR,__HIDDEN_FIELDS__");

        TradeDataMqManager.getInstance().sendMsg("ORD.EXCH." + getClientOrderId(), data, false,null);
    }

    @Override
    public SessionID getSessionID() {
        return sessionID;
    }

    @Override
    public void setSessionID(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public ExchangeOrder(ClientOrder parentOrder, SessionID exchangeSessionID,double qtyAllocated,
                         double price,AllocationDecisionType decisionType,AllocationCategory category){
        setClientOrderId(UUID.randomUUID().toString());
        this.setOrderQty((long) qtyAllocated);
        this.setPrice(price);
        this.parent = parentOrder;
        this.decisionType = decisionType;
        this.allocationCategory = category;

        if(this.parent!=null){
            this.setOrderSide(parentOrder.getOrderSide());
            this.setSymbol(parentOrder.getSymbol());
        }
        else{
            this.setSymbol("N/A");
            this.setOrderSide(new Side(Side.BUY));
        }

        this.setSessionID(exchangeSessionID);
    }

    public ClientOrder getParent() {
        return parent;
    }

    @Override
    public void processExecutionReport(ExecutionReport report) throws Exception {
        ClientOrder clientOrder = this.getParent();
        ExecType execType = report.getExecType();
        OrderHandler orderHandler = clientOrder.getOrderHandler();
        DisruptorController controller = null ;
        if(orderHandler!=null){
            controller = orderHandler.getController();
        }


        switch (execType.getValue()){
            case ExecType.NEW:
                LogFactory.omLog("EXG -> New Ack", clientOrder.getClientOrderId(), report);
                this.setOrdStatus(new OrdStatus(OrdStatus.NEW));

                if(clientOrder.getOrdStatus().getValue()!=OrdStatus.PENDING_CANCEL
                        && clientOrder.getOrdStatus().getValue()!=OrdStatus.PARTIALLY_FILLED
                        && clientOrder.getOrdStatus().getValue()!=OrdStatus.FILLED
                        && clientOrder.getOrdStatus().getValue()!=OrdStatus.CANCELED
                        && clientOrder.getOrdStatus().getValue()!=OrdStatus.REJECTED){
                    //haven't been notified with 'new ack'
                    clientOrder.setOrdStatus(new OrdStatus(OrdStatus.NEW));

                    ExecutionReport ackClientOrder = (ExecutionReport)report.clone();
                    ackClientOrder.set(new ClOrdID(clientOrder.getClientOrderId()));
                    FixMsgHelper.sendMessage(ackClientOrder, clientOrder.getSessionID(),
                            FixMsgHelper.CLIENT_NEW_ACK_LOG, clientOrder.getClientOrderId());
                }

                break;

            case ExecType.REJECTED :
                try{
                    LogFactory.omLog(FixMsgHelper.EXG_OUT_REJECTED, clientOrder.getClientOrderId(), report);

                    this.setOrdStatus(new OrdStatus(OrdStatus.REJECTED));

                    //todo: should calculate how many times rejected
                    if(this.getParent().getOrderHandler().isPeggingOrder()==true && controller!=null ){
                        controller= orderHandler.getController();
                        if(controller!=null){
                            controller.enqueueEvent(com.csc108.disruptor.event.EventType.EVALUATION,orderHandler,null);
                        }
                    }

                }catch (Exception ex){
                    LogFactory.error("Process REJECTED response error!",ex);
                }
                break;

            case ExecType.PENDING_NEW :
                try{
                    LogFactory.omLog(FixMsgHelper.EXG_OUT_PENDING_NEW, clientOrder.getClientOrderId(), report);
                }catch (Exception ex){
                    LogFactory.error("Process pending new response error!",ex);
                }
                break;

            case ExecType.PARTIAL_FILL :
                try{
                    LogFactory.omLog(FixMsgHelper.EXG_OUT_PARTIAL_FILL,clientOrder.getClientOrderId(),report);
                    double lastPx =report.getField(new LastPx()).getValue();
                    Double lastFillShares = report.getField(new LastShares()).getValue();
                    this.setLastPrice(lastPx);
                    this.setLastShares(lastFillShares);

                    //calcualte avg price
                    if(this.getLastShares()==0){
                        this.setAvgPrice(this.getLastPrice());
                    }else{
                        double totalAmt = this.getCumQty()*this.getAvgPrice()+this.getLastShares()*this.getLastPrice();
                        double totalQty = this.getCumQty()+this.getLastShares();
                        this.setAvgPrice(totalAmt/totalQty);
                    }

                    CumQty cumQty = new CumQty();
                    report.get(cumQty);
                    this.setCumQty((long)cumQty.getValue());

                    //update exchange order qty
                    if(this.getOrdStatus().getValue()!=OrdStatus.PENDING_CANCEL
                            && this.getOrdStatus().getValue()!=OrdStatus.CANCELED){
                        this.setOrdStatus(new OrdStatus(OrdStatus.PARTIALLY_FILLED));
                    }else{
                        if(this.getLeavesQty()==0){
                            //has been requested to cancel previously
                            this.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));

                            if(this.getParent().getOrderHandler().isPeggingOrder()==true && controller!=null){
                                controller= orderHandler.getController();
                                if(controller!=null){
                                    controller.enqueueEvent(com.csc108.disruptor.event.EventType.EVALUATION, orderHandler, null);
                                }
                            }
                        }
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                    LogFactory.error("Process partial fill response error!",ex);
                }
                break;

            case ExecType.FILL :
                try{
                    LogFactory.omLog(FixMsgHelper.EXG_OUT_FILL,clientOrder.getClientOrderId(),report);
                    //update exchange order qty
                    double lastPx =report.getField(new LastPx()).getValue();
                    Double lastFillShares = report.getField(new LastShares()).getValue();
                    this.setLastPrice(lastPx);
                    this.setLastShares(lastFillShares);

                    //calcualte avg price
                    if(this.getLastShares()==0){
                        this.setAvgPrice(this.getLastPrice());
                    }else{
                        double totalAmt = this.getCumQty()*this.getAvgPrice()+this.getLastShares()*this.getLastPrice();
                        double totalQty = this.getCumQty()+this.getLastShares();
                        this.setAvgPrice(totalAmt/totalQty);
                    }

                    CumQty cumQty = new CumQty();
                    report.get(cumQty);
                    this.setCumQty((long)cumQty.getValue());

                    if(this.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL){
                        //reject the original cancel request
                        //FixMsgHelper.rejectCancelRequestToClient();
                        this.setOrderSatusForcely(new OrdStatus(OrdStatus.FILLED));

                        Alert.fireAlert(Alert.Severity.Info,
                                String.format(Alert.PROCESS_EXECUTION_REPORT_ERROR,this.getClientOrderId()),
                                "Response filled, but this slices has been requested to cancel before!",null);
                    }

                    if(this.getParent().getOrderHandler().isPeggingOrder()==true && controller!=null){
                        controller= orderHandler.getController();
                        if(controller!=null){
                            controller.enqueueEvent(com.csc108.disruptor.event.EventType.EVALUATION,orderHandler,null);
                        }
                    }

                    this.setOrdStatus(new OrdStatus(OrdStatus.FILLED));
                }catch (Exception ex){
                    ex.printStackTrace();
                    LogFactory.error("Process fill response error!",ex);
                }
                break;

            case ExecType.PENDING_CANCEL :
                LogFactory.omLog(FixMsgHelper.EXG_OUT_PENDING_CANCEL,clientOrder.getClientOrderId(),report);
                break;

            case ExecType.RESTATED:
                try {
                    StrategyStatusType strategyStatusType = new StrategyStatusType();
                    int type= report.getInt(strategyStatusType.getField());
                    if(type==1){
                        LogFactory.omLog(FixMsgHelper.EXG_IN_RESUME_ORDER+" [resumed]",clientOrder.getClientOrderId(),report);
                        this.setPauseResumeState(OrderState.RESUMED);

                    }else if(type==2){
                        LogFactory.omLog(FixMsgHelper.EXG_IN_RESUME_ORDER+" [paused]",clientOrder.getClientOrderId(),report);
                        this.setPauseResumeState(OrderState.PAUSED);
                    }else if(type==3){
                        LogFactory.omLog(FixMsgHelper.EXG_IN_RESUME_ORDER+" [pause with cancel]",clientOrder.getClientOrderId(),report);
                        this.setPauseResumeState(OrderState.PAUSED);
                    }else{
                        this.setPauseResumeState(OrderState.UNKNOWN);
                        LogFactory.omLog(FixMsgHelper.EXG_IN_RESUME_ORDER,clientOrder.getClientOrderId(),report);
                    }

                }catch (Exception ex){
                    LogFactory.error("Processing restatement error!",ex);
                }
                break;

            case ExecType.EXPIRED :
                try{
                    LogFactory.omLog(FixMsgHelper.EXG_OUT_EXPIRED,clientOrder.getClientOrderId(),report);
                    this.setOrdStatus(new OrdStatus(OrdStatus.EXPIRED));
                }catch (Exception ex){
                    LogFactory.error("Process expired response error!",ex);
                }

                break;

            case ExecType.CANCELED:
                LogFactory.omLog(FixMsgHelper.EXG_OUT_CANCELED,clientOrder.getClientOrderId(),report);
                CumQty cumQty = new CumQty();
                report.get(cumQty);

                this.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
                this.setCumQty((long)cumQty.getValue());

                if(cumQty.getValue()==this.getCumQty()){
                    if(this.getLeavesQty()>0){
                        //exchangeOrder.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
                    }else{
                        //should be filled already
                        //exchangeOrder.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
                        Alert.fireAlert(Alert.Severity.Minor,
                                String.format(Alert.PROCESS_EXECUTION_REPORT_ERROR, this.getClientOrderId()),
                                "Response canceled, but leaves qty is not zero!", null);
                    }
                }else{
                    Alert.fireAlert(Alert.Severity.Minor,
                            String.format(Alert.PROCESS_EXECUTION_REPORT_ERROR,this.getClientOrderId()),
                            "Response canceled, but canceled cum qty is not equal to already cum qty!",null);
                }

                if(this.getParent().getOrderHandler().isPeggingOrder()==true && controller!=null){
                    controller= orderHandler.getController();
                    if(controller!=null){
                        controller.enqueueEvent(com.csc108.disruptor.event.EventType.EVALUATION, orderHandler, null);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Can't handle execuion report type:"+execType.getValue());

        }

        if(this.getParent().getOrderHandler().isReportProgressNeeded()==true){
            reportProgress();
        }
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice1){
        avgPrice= avgPrice1;
    }

    public AllocationCategory getAllocationCategory() {
        return allocationCategory;
    }

    public AllocationDecisionType getDecisionType(){
        return decisionType;
    }
}
