package com.csc108.model.fixModel.order;

import com.csc108.log.LogFactory;
import com.csc108.model.OrderState;
import com.csc108.model.cache.AlgoTimeSeriesDataCache;
import com.csc108.model.cache.TradeDataMqManager;
import com.csc108.utility.FixMsgHelper;
import com.csc108.utility.FixUtil;
import com.csc108.utility.FormattedTable;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zhangbaoshan on 2016/4/28.
 */
public class ClientOrder implements Serializable {

    private static final long serialVersionUID = -2174356994537890017L;

    private String orderType;
    private String stockName;

    private String originalClientOrderId;
    private String clientOrderId;
    private String cancelRequestClientOrderId;
    private String symbol="N/A";

    private String accountId="";
    private String clientId="";

    private String secondaryCloId="";
    private String exchangeDest="";
    private String securityType="";

    private OrdStatus ordStatus = new OrdStatus(OrdStatus.PENDING_NEW);
    private OrderState orderState = OrderState.UNKNOWN;
    private Side orderSide = new Side(Side.BUY);

    private double price=0;
    private double lastPrice;
    private long orderQty;
    private long cumQty;
    private double lastShares;
    //private double leavesQty;

    private LocalDateTime  effectiveTime =null;// LocalDateTime.now().minusMinutes(5);
    private LocalDateTime  expireTime = null;//LocalDateTime.now().plusMinutes(5);

    private long adv20=0;
    private long mdv21 = 0;

    private NewOrderSingle newOrderRequestMsg;
    private OrderCancelRequest cancelRequestMsg;

    private double participationRate=-1.0;
    //link to it's manager
    private OrderHandler orderHandler;

    //cache all of incoming execution report not handled yet
    //once handled, will be moved to handled queue
    private final ConcurrentLinkedQueue<ExecutionReport> unhandledExecutionReports = new ConcurrentLinkedQueue<>();

    //cache all of handled execution report
    private final ConcurrentLinkedQueue<ExecutionReport> handledExecutionReports= new ConcurrentLinkedQueue<>();

    //cache all of transactions of timeseries
    private final AlgoTimeSeriesDataCache<OrderSnapshot> transactions = new AlgoTimeSeriesDataCache<>();

    private SessionID sessionID;

    private OrderState pauseResumeState = OrderState.UNKNOWN;
    public OrderState getPauseResumeState() {
        return pauseResumeState;
    }
    public void setPauseResumeState(OrderState pauseResumeState) {
        this.pauseResumeState = pauseResumeState;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getCancelRequestClientOrderId() {
        return cancelRequestClientOrderId;
    }

    public void setCancelRequestClientOrderId(String cancelRequestClientOrderId) {
        this.cancelRequestClientOrderId = cancelRequestClientOrderId;
    }

    public OrdStatus getOrdStatus() {
        return ordStatus;
    }

    private OrdStatus previousStatusBeforePendingCancel;
    public void setOrdStatus(OrdStatus ordStatus){
        if(FixUtil.IsOrderCompleted(this.getOrdStatus())==true && FixUtil.IsOrderCompleted(ordStatus)==false)
            throw new IllegalArgumentException("can't set a completed order from "+this.getFixStatusDisplay()+" to " + ordStatus.getValue());
        //cache original ordstatus in case cancel being rejected and restore the original status
        if(ordStatus.getValue()==OrdStatus.PENDING_CANCEL && this.ordStatus.getValue()!=OrdStatus.PENDING_CANCEL){
            previousStatusBeforePendingCancel = this.ordStatus;
        }

        //set previous status as the current ord status in case cancel request sent out too soon
        if(this.ordStatus.getValue()==OrdStatus.PENDING_CANCEL ){
            if(ordStatus.getValue()!=OrdStatus.PENDING_CANCEL){
                previousStatusBeforePendingCancel = ordStatus;
            }
        }

        if(FixUtil.convertable(this.getOrdStatus(),ordStatus)==true){
            this.ordStatus = ordStatus;
        }
    }

    public void setOrderSatusForcely(OrdStatus ordStatusByForce){
        this.ordStatus = ordStatusByForce;
    }

    //restore to previous order status
    public void restoreOrderStatusBeforeCancel(){
        if(previousStatusBeforePendingCancel==null)
            throw new IllegalArgumentException("Prev ordstatus is null before being canceled!");

        if(FixUtil.IsOrderCompleted(this.ordStatus))
            throw new IllegalArgumentException("Current ordstauts is completed as "+this.ordStatus.getValue()+", can't restore");

        this.ordStatus = previousStatusBeforePendingCancel;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double avgPrice) {
        this.price= avgPrice;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void setLastShares(double lastShares){
        this.lastShares=lastShares;
    }

    public double getLastShares(){
        return this.lastShares;
    }

    public long getOrderQty() {
        return orderQty;
    }

    public void setOrderQty(long orderQty) {
        this.orderQty = orderQty;
    }

    public long getCumQty() {
        return cumQty;
    }

    public void setCumQty(long cumQty) {
        this.cumQty = cumQty;
    }

    public double getLeavesQty() {
        return orderQty - cumQty;
    }

    public LocalDateTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public LocalDateTime  getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public NewOrderSingle getNewOrderRequestMsg() {
        return newOrderRequestMsg;
    }

    public OrderCancelRequest getCancelRequestMsg() {
        return cancelRequestMsg;
    }

    public void setCancelRequestMsg(OrderCancelRequest cancelRequestMsg)
    {
        this.cancelRequestMsg = cancelRequestMsg;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public ClientOrder(){
        //this.algoOrderID="N/A";
        this.orderHandler =null;
        this.setClientOrderId(UUID.randomUUID().toString());
        this.setOrdStatus(new OrdStatus(OrdStatus.STOPPED));
    }

    public ClientOrder(NewOrderSingle message,SessionID clientSessionID) throws Exception {
        this.setSessionID(clientSessionID);

        this.newOrderRequestMsg = (NewOrderSingle)message;

        ClOrdID clOrdID = new ClOrdID();
        this.newOrderRequestMsg.get(clOrdID);

        this.setClientOrderId(clOrdID.getValue().toString());
    }


    public String getSymbol() {
        return symbol;
    }

    public String getSecurityId() {
        return symbol+"."+exchangeDest.trim();
    }


    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    public Side getOrderSide() {
        return orderSide;
    }

    public void setOrderSide(Side orderSide) {
        this.orderSide = orderSide;
    }

    public String getFixStatusDisplay() {
        if (this.getOrdStatus().getValue() == '0') return "New";
        if (this.getOrdStatus().getValue() == '1') return "PartialFilled";
        if (this.getOrdStatus().getValue() == '2') return "Filled";
        if (this.getOrdStatus().getValue() == '3') return "Done for day";
        if (this.getOrdStatus().getValue() == '4') return "Canceled";
        if (this.getOrdStatus().getValue() == '5') return "Replaced";
        if (this.getOrdStatus().getValue() == '6') return "PendingCancel";
        if (this.getOrdStatus().getValue() == '7') return "Stopped";
        if (this.getOrdStatus().getValue() == '8') return "Rejected";
        if (this.getOrdStatus().getValue() == '9') return "Suspended";
        if (this.getOrdStatus().getValue() == 'A') return "PendingNew";
        if (this.getOrdStatus().getValue() == 'B') return "Calculated";
        if (this.getOrdStatus().getValue() == 'C') return "Expired";
        if (this.getOrdStatus().getValue() == 'D') return "Accepted for bidding";
        if (this.getOrdStatus().getValue() == 'E') return "Pending Replace";
        return "<UNKNOWN>";
    }

    public String getOrderStateDisplay() {
        if (this.getOrderState().getValue() == 0) return "Initialized";
        if (this.getOrderState().getValue() == 1) return "SENT_TO_EXCHANGE";
        if (this.getOrderState().getValue() == 2) return "Completed";
        if (this.getOrderState().getValue() == 4) return "Pending_Pause";
        if (this.getOrderState().getValue() == 5) return "PAUSED";
        if (this.getOrderState().getValue() == 6) return "PENDING_RESUME";
        if (this.getOrderState().getValue() == 7) return "RESUMED";
        if (this.getOrderState().getValue() == 10) return "Frozen";
        return "<UNKNOWN>";
    }

    public static String toString(ArrayList<ExchangeOrder> orders ){
        FormattedTable table = new FormattedTable();
        List<Object> header = new ArrayList<Object>();
        header.add("clOrdId");
        header.add("side");
        header.add("price");
        header.add("orderQty");
        header.add("cumQty");
        header.add("leavesQty");
        header.add("symbol");
        header.add("orderStatus");
        header.add("orderState");
        header.add("accountId");
        table.AddRow(header);

        if(orders!=null && orders.size()>0){
            orders.forEach(order->{
                try{
                    List<Object> row = new ArrayList<Object>();
                    row.add(order.getClientOrderId());
                    row.add(order.getOrderSide().getValue());
                    row.add(Double.toString(order.getPrice()));
                    row.add(Double.toString(order.getOrderQty()));
                    row.add(Double.toString(order.getCumQty()));
                    row.add(Double.toString(order.getLeavesQty()));
                    row.add(order.getSymbol());
                    row.add(order.getFixStatusDisplay());
                    row.add(order.getOrderStateDisplay());
                    row.add(order.getAccountId());

                    table.AddRow(row);
                }catch (Exception ex){
                    LogFactory.error("Parse error!",ex);
                }
            });
            return table.toString();
        }
        return "N/A";
    }

    @Override
    public String toString(){
        try{
            FormattedTable table = new FormattedTable();
            List<Object> row = new ArrayList<Object>();
            row.add("clOrdId");
            row.add("side");
            row.add("price");
            row.add("orderQty");
            row.add("cumQty");
            row.add("leavesQty");
            row.add("symbol");
            row.add("orderStatus");
            row.add("orderState");
            row.add("accountId");
            table.AddRow(row);

            row = new ArrayList<Object>();
            row.add(this.getClientOrderId());
            row.add(this.getOrderSide().getValue());

            row.add(Double.toString(this.getPrice()));
            row.add(Double.toString(this.getOrderQty()));
            row.add(Double.toString(this.getCumQty()));
            row.add(Double.toString(this.getLeavesQty()));


            row.add(this.getSymbol());
            row.add(this.getFixStatusDisplay());
            row.add(this.getOrderStateDisplay());
            row.add(this.getAccountId());

            table.AddRow(row);

            return table.toString();
        }catch (Exception ex){
            LogFactory.error("Parse error!",ex);
            return "parse error"+ex.getMessage();
        }
    }

    public OrderHandler getOrderHandler() {
        return orderHandler;
    }

    public void setOrderHandler(OrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSecondaryCloId() {
        return secondaryCloId;
    }

    public void setSecondaryCloId(String secondaryCloId) {
        this.secondaryCloId = secondaryCloId;
    }

    public String getExchangeDest() {
        return exchangeDest;
    }

    public void setExchangeDest(String exchangeDest) {
        this.exchangeDest = exchangeDest;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    private OrdStatus expectClientOrderStatus(){
        int totalExchangeOrders = this.getOrderHandler().getExchangeOrders().size();

        if(totalExchangeOrders==0)
            return new OrdStatus(OrdStatus.PENDING_NEW);

        long totalFilled=this.getOrderHandler().getExchangeOrders().stream().
                filter(x->x.getOrdStatus().getValue()==OrdStatus.FILLED).count();

        long totalExpired=this.getOrderHandler().getExchangeOrders().stream().
                filter(x -> x.getOrdStatus().getValue() == OrdStatus.EXPIRED).count();

        long totalRejected=this.getOrderHandler().getExchangeOrders().stream().
                filter(x -> x.getOrdStatus().getValue() == OrdStatus.REJECTED).count();

        long totalCanceled=this.getOrderHandler().getExchangeOrders().stream().
                filter(x -> x.getOrdStatus().getValue() == OrdStatus.CANCELED).count();

        if(totalExpired==totalExchangeOrders)
            return new OrdStatus(OrdStatus.EXPIRED);

        if(totalRejected==totalExchangeOrders)
            return new OrdStatus(OrdStatus.REJECTED);

        if(totalFilled==totalExchangeOrders)
            return new OrdStatus(OrdStatus.FILLED);

        if(totalCanceled==totalExchangeOrders)
            return new OrdStatus(OrdStatus.CANCELED);

        return new OrdStatus(OrdStatus.PENDING_NEW);
    }

    private void reportProgress(){
        if(this.getOrderHandler().isPeggingOrder()==false){
            return;
        }

        try{
            OrderSnapshot snapshot = OrderSnapshot.click(this);
            transactions.put(this.getClientOrderId(),snapshot);
            String timeSeries = transactions.outputString(getClientOrderId());

            String evaTopic= "ENG.EVA." + getClientOrderId();
            HashMap<String,String> exchData = new HashMap<>();
            exchData.put("cumCurve",timeSeries);

            //buildup max bond curve, only for monitor display char purpose
            String maxCurve = String.format("%s=%s,%s=%s",this.getEffectiveTime().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")),0.0,
                    this.getExpireTime().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")),0.0);
            exchData.put("maxBound",maxCurve);

            TradeDataMqManager.getInstance().sendMsg(evaTopic, exchData, false, null);

        }catch (Exception ex){
            LogFactory.error("Snapshot and publish msg error!",ex);
        }
    }

    public void processExecutionReport(ExecutionReport executionReport) throws Exception {
        try{
            ExecutionReport responseReport=null;
            if(executionReport==null){
                responseReport = new ExecutionReport(
                        new OrderID(this.getClientOrderId()),
                        new ExecID(UUID.randomUUID().toString()),
                        new ExecTransType(ExecTransType.NEW),
                        new ExecType(ExecType.FILL),//new ExecType(ExecType.FILL),
                        this.getOrdStatus() , //new OrdStatus(OrdStatus.FILLED),
                        new Symbol(this.getSymbol()),
                        this.getOrderSide(),
                        new LeavesQty(this.getLeavesQty()),
                        new CumQty(this.getCumQty()),
                        new AvgPx(this.getPrice())
                );
            }else{
                responseReport = (ExecutionReport)executionReport.clone();
            }

            //handle pause/resume report firstly
            ExecType execType = responseReport.getExecType();
            if(execType.getValue()==ExecType.RESTATED){
                FixMsgHelper.responseRestatementReport(responseReport,this);
                return;
            }

            long cumQtyFromExchange = 0;
            if(this.getOrderHandler().getExchangeOrders().size()>0){
                cumQtyFromExchange= this.getOrderHandler().getExchangeOrders().stream()
                        .mapToLong(x -> x.getCumQty()).sum();
            }

            if(cumQtyFromExchange>this.getCumQty()){
                //should supplement a partial fill or fill
                //calculate last price by weighted avg
                double lastPxTotalWeightedFromExchange = this.getOrderHandler().getExchangeOrders().stream().mapToDouble(x->x.getLastPrice()*x.getLastShares()).sum();
                double lastFillSharesTotalFromExchange = this.getOrderHandler().getExchangeOrders().stream().mapToDouble(x->x.getLastShares()).sum();
                double lastPxWeighted = lastFillSharesTotalFromExchange>0?lastPxTotalWeightedFromExchange/lastFillSharesTotalFromExchange:0;

                //fixModel bug: when no partial fiiled happend, the lastPxWeighted would be 0, to avoid it, use
                // order price instead
                if(Double.compare(lastPxWeighted,0)==0){
                    lastPxWeighted = this.getPrice();
                }
                this.setLastPrice(lastPxWeighted);

                long lastShares = cumQtyFromExchange-this.getCumQty();
                this.setLastShares(lastShares);
                this.setCumQty(this.getCumQty() + lastShares);

                if(this.getLeavesQty()==0){
                    //filled

                    responseReport.set( new OrderID(this.getClientOrderId()));
                    responseReport.set( new ExecID(UUID.randomUUID().toString()));
                    responseReport.set( new ExecTransType(ExecTransType.NEW));
                    responseReport.set( new ExecType(ExecType.FILL));
                    responseReport.set( new OrdStatus(OrdStatus.FILLED));
                    responseReport.set( new LeavesQty(this.getLeavesQty()));
                    responseReport.set( new CumQty(this.getCumQty()));
                    responseReport.set( new AvgPx(this.getPrice()));
                    responseReport.set(new LastPx(this.getLastPrice()));
                    responseReport.set(new LastShares(this.getLastShares()));

                    //being requested to cancel before
                    if(this.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL){
                        //if there is a cancel request, reject it since order has been filled
                        FixMsgHelper.rejectCancelRequestToClient(this, this.getCancelRequestMsg(), "Original order has been filled!");
                    }
                    FixMsgHelper.handelFillClientOrder(this, responseReport);
                    this.setOrdStatus(new OrdStatus(OrdStatus.FILLED));
                }else{
                    //Partial filled

                    responseReport.set( new OrderID(this.getClientOrderId()));
                    responseReport.set( new ExecID(UUID.randomUUID().toString()));
                    responseReport.set( new ExecTransType(ExecTransType.NEW));
                    responseReport.set( new ExecType(ExecType.PARTIAL_FILL));
                    responseReport.set( new OrdStatus(OrdStatus.PARTIALLY_FILLED));
                    responseReport.set( new LeavesQty(this.getLeavesQty()));
                    responseReport.set( new CumQty(this.getCumQty()));
                    responseReport.set( new AvgPx(this.getPrice()));

                    responseReport.set(new LastPx(this.getLastPrice()));
                    responseReport.set(new LastShares(this.getLastShares()));
                    FixMsgHelper.handelPartialFillClientOrder(this, responseReport);

                    if(this.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL){
                        //If there is a cancel request
                        if(this.getOrderHandler().noActiveExchangeOrders()==true){
                            //canceled it since all of exchange order has been completed and leaves qty>0
                            this.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
                            FixMsgHelper.responseCancelRequestClientOrder(this, this.getOrdStatus(),
                                    new ExecType(ExecType.CANCELED), FixMsgHelper.CLIENT_IN_REPORT_CANCELED, this.getCancelRequestMsg());
                        }
                    }else{
                        this.setOrdStatus(new OrdStatus(OrdStatus.PARTIALLY_FILLED));
                    }
                }
            }else if(this.getOrderHandler().noActiveExchangeOrders() ==true
                    && FixUtil.IsOrderCompleted(this.getOrdStatus())==false){
                if( this.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL ){
                    //canceled it since all of exchange order has been completed and leaves qty>0
                    this.setOrdStatus(new OrdStatus(OrdStatus.CANCELED));
                    FixMsgHelper.responseCancelRequestClientOrder(this, this.getOrdStatus(),
                            new ExecType(ExecType.CANCELED), FixMsgHelper.CLIENT_IN_REPORT_CANCELED, this.getCancelRequestMsg());
                }else{
                    //expired client order since there is nothing to do any longer
                    if(this.getOrderHandler().isPeggingOrder()==false){
                        OrdStatus ordStatusExpected = expectClientOrderStatus();
                        this.setOrdStatus(ordStatusExpected);

                        if(ordStatusExpected.getValue()==OrdStatus.CANCELED){
                            //it's a cancelled directly from exchange
                            responseReport.setField(new OrigClOrdID(this.getClientOrderId()));
                        }
                        FixMsgHelper.responseClientOrderWhenCompleted(this,responseReport);
                    }else{
                        //todo: check expire logic
                    }
                }
            }

            if(this.getOrderHandler().isPeggingOrder()==true){
                reportProgress();
            }
        }catch (Exception ex){
            ex.printStackTrace();
            LogFactory.error("Process client order error!", ex);
        }
    }

    public static String printOut(ArrayList<ExchangeOrder> clientOrders){
        try{
            FormattedTable table = new FormattedTable();
            List<Object> row = new ArrayList<Object>();
            row.add("clOrdId");
            row.add("side");
            row.add("price");
            row.add("orderQty");
            row.add("cumQty");
            row.add("leavesQty");
            row.add("symbol");
            row.add("orderStatus");
            row.add("orderState");
            row.add("accountId");
            table.AddRow(row);

            for (ClientOrder clientOrder:clientOrders){
                row = new ArrayList<Object>();
                row.add(clientOrder.getClientOrderId());
                row.add(clientOrder.getOrderSide().getValue());
                row.add(Double.toString(clientOrder.getPrice()));
                row.add(Double.toString(clientOrder.getOrderQty()));
                row.add(Double.toString(clientOrder.getCumQty()));
                row.add(Double.toString(clientOrder.getLeavesQty()));
                row.add(clientOrder.getSymbol());
                row.add(clientOrder.getFixStatusDisplay());
                row.add(clientOrder.getOrderStateDisplay());
                row.add(clientOrder.getAccountId());
                table.AddRow(row);
            }
            return table.toString();
        }catch (Exception ex){
            LogFactory.error("Parse error!",ex);
            return "parse error"+ex.getMessage();
        }
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void setParticipationRate(double rate) {
        this.participationRate = rate;
    }

    public double getParticipationRate(){
        return this.participationRate;
    }

    public long getAdv20() {
        return adv20;
    }

    public void setAdv20(long adv20) {
        this.adv20 = adv20;
    }

    public long getMdv21() {
        return mdv21;
    }

    public void setMdv21(long mdv20) {
        this.mdv21 = mdv20;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
