package com.csc108.model.fixModel.order;

import com.csc108.configuration.GlobalConfig;
import com.csc108.decision.DecisionChainManager;
import com.csc108.decision.IDecisionConfig;
import com.csc108.decision.configuration.PegConfiguration;
import com.csc108.disruptor.concurrent.DisruptorController;
import com.csc108.disruptor.event.*;
import com.csc108.disruptor.event.EventType;
import com.csc108.log.LogFactory;
import com.csc108.model.Allocation;
import com.csc108.model.IDataHandler;
import com.csc108.model.OrderState;
import com.csc108.model.cache.*;
import com.csc108.model.criteria.*;
import com.csc108.model.fixModel.sessionPool.ISessionPoolPicker;
import com.csc108.model.market.*;
import com.csc108.tradingRule.providers.HandlerProvider;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import com.csc108.utility.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.OrderPauseResumeRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhangbaoshan on 2016/5/6.
 */
public class OrderHandler implements IDataHandler {
    //for check if always use same thread to process same client order
    long originalThreadId;

    private LocalDateTime transactionTime =LocalDateTime.MIN;

    private RealTimeMarketData realTimeMarketData;
    private IntervalMarketData intervalMarketData;
    private AllDayIntervalMarketData allDayIntervalMarketData;

    //for performance test
    private long createdTimeStamp;
    private long enqueueTimeStamp;
    private long handledTimeStamp;
    private LocalDateTime lastProcessedTime = LocalDateTime.MIN;
    private LocalDateTime lastPeggingTime = LocalDateTime.MIN;

    //if this is a conditional order
    private boolean conditionalOrder ;
    private Condition condition;

    //hold for all of decisions configuration data
    private DecisionConfigCache configCache = new DecisionConfigCache();

    private final ClientOrder clientOrder;
    private final DisruptorController controller;
    public DisruptorController getController(){
        return controller;
    }

    private PegConfiguration pegConfiguration = null;
    public PegConfiguration getPegConfiguration(){
        return pegConfiguration;
    }
    public boolean isPeggingOrder() {
        return  pegConfiguration==null?false:true;
    }

    private DecisionChainManager decisionChain=null;
    public void setDecisionChain(DecisionChainManager chainManager){
        decisionChain = chainManager;
    }
    public DecisionChainManager getDecisionChain(){
        return decisionChain;
    }

    private ISessionPoolPicker sessionPoolPicker;
    public ISessionPoolPicker getSessionPoolPicker() {
        return sessionPoolPicker;
    }

    public void setSessionPoolPicker(ISessionPoolPicker sessionPoolPicker) {
        this.sessionPoolPicker = sessionPoolPicker;
    }

    /*hold how many allocations have been calculated out for this client order*/
    private final ArrayList<Allocation> allocations = new ArrayList<>();

    /*OR mapping for master-child relationship orders */
    private final ArrayList<ExchangeOrder> exchangeOrders = new ArrayList<>();
    public ArrayList<ExchangeOrder> getExchangeOrders(){
        return exchangeOrders;
    }

    private HashMap<String,IDecisionConfig> decisionConfigHashMap = new HashMap<>();
    public HashMap<String,IDecisionConfig> getDecisionConfigHashMap(){
        return decisionConfigHashMap;
    }

    public boolean noActiveExchangeOrders(){
        if(this.getExchangeOrders().size()==0)
            return true;

        if(this.getExchangeOrders().stream().
                filter(x->FixUtil.IsOrderCompleted(x.getOrdStatus())==false).count()!=0)
            return false;
        return true;
    }

    private boolean hasPendingCancelExchangeOrders(){
        if(this.getExchangeOrders().size()==0)
            return false;

        if(this.getExchangeOrders().stream().
                filter(x->x.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL).count()!=0)
            return true;

        return false;
    }

    public OrderHandler(ClientOrder order,DisruptorController controller){
        this.clientOrder = order;
        order.setOrderHandler(this);
        this.controller = controller;
        //this.configCache.put(this.getClientOrder().getClientOrderId(),new SplitOrderDecisionConfig(1));
    }

    public ClientOrder getClientOrder() {
        return clientOrder;
    }

    @Override
    public String getID() {
        return this.getClientOrder().getClientOrderId();
    }

    public String getAlertID(){
        String exchangeId = "Unknown";
        if(this.getExchangeOrders().size()>0){
            exchangeId = this.getExchangeOrders().get(0).getClientOrderId();
        }

        return String.format("[%s]<->[%s]", this.getClientOrder().getClientOrderId(), exchangeId);
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public long getEnqueueTimeStamp() {
        return enqueueTimeStamp;
    }

    public void setEnqueueTimeStamp(long enqueueTimeStamp) {
        this.enqueueTimeStamp = enqueueTimeStamp;
    }

    public long getHandledTimeStamp() {
        return handledTimeStamp;
    }

    public void setHandledTimeStamp(long handledTimeStamp) {
        this.handledTimeStamp = handledTimeStamp;
    }

    @Override
    public void setOriginalThread(long threadID) {
        this.originalThreadId = threadID;
    }

    @Override
    public long getOriginalThreadId() {
        return this.originalThreadId;
    }

    public DecisionConfigCache getConfigCache() {
        return configCache;
    }

    //initialize new order
    public void initialize(){
        try{
            if(  this.getClientOrder().getNewOrderRequestMsg().isSetField(109)){
                this.getClientOrder().setClientId(this.getClientOrder().getNewOrderRequestMsg().getString(109));
            }else{
                if (this.getClientOrder().getNewOrderRequestMsg().isSetField(1))
                    this.getClientOrder().setClientId(this.getClientOrder().getNewOrderRequestMsg().getString(1));
            }
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.FIELD_NOT_FOUND_KEY,"Client ID",this.getID()),
                    this.getClientOrder().getNewOrderRequestMsg().toString(),ex);
        }

        //set order qty

        try{
            OrderQty orderQty= new OrderQty();
            this.getClientOrder().getNewOrderRequestMsg().get(orderQty);
            this.getClientOrder().setOrderQty((long)orderQty.getValue());
            this.getClientOrder().setCumQty(0);
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Critical,String.format(Alert.FIELD_NOT_FOUND_KEY,"Order quantity",this.getID()),
                    this.getClientOrder().getNewOrderRequestMsg().toString(),ex);
        }

        //set price
        try{
            Price price = new Price();
            this.getClientOrder().getNewOrderRequestMsg().get(price);
            this.getClientOrder().setPrice(price.getValue());
        }catch (FieldNotFound ex){
            //LogFactory.error("Invalid price",ex);
        }

        //set side
        try{
            Side orderSide= this.getClientOrder().getNewOrderRequestMsg().getSide();
            this.getClientOrder().setOrderSide(orderSide);
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.FIELD_NOT_FOUND_KEY,"Symbol",this.getID()),
                    this.getClientOrder().getNewOrderRequestMsg().toString(),ex);
        }

        //set account
        try{
            String accountId="";
            if(this.getClientOrder().getNewOrderRequestMsg().isSetField(7001)){
                accountId ="0000"+ this.getClientOrder().getNewOrderRequestMsg().getString(7001);
            }else{
                accountId =this.getClientOrder().getNewOrderRequestMsg().getString(1);
            }
            this.getClientOrder().setAccountId(accountId);

        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.FIELD_NOT_FOUND_KEY,"AccountID(Tag1)",this.getID()),
                    this.getClientOrder().getNewOrderRequestMsg().toString(),ex);
        }

        try{
            String secondaryCloId =this.getClientOrder().getNewOrderRequestMsg().getString(526);
            this.getClientOrder().setSecondaryCloId(secondaryCloId);

        }catch (Exception ex){
            LogFactory.error(String.format("SecondaryCloId (Tag526) not specified @[%s] %n",this.getClientOrder().getClientOrderId()),ex);
        }

        //set type
        try{
            OrdType ordType=new OrdType();
            this.getClientOrder().getNewOrderRequestMsg().get(ordType);
            if(ordType.getValue()==OrdType.MARKET)
                this.getClientOrder().setOrderType("MARKET");
            else if(ordType.getValue()==OrdType.LIMIT)
                this.getClientOrder().setOrderType("LIMIT");
            else
                this.getClientOrder().setOrderType("UNKNOWN");

        }catch (Exception ex){
            //gFactory.error("Invalid order type",ex);
        }

        //set participation rate
        try{
            if(this.getClientOrder().getNewOrderRequestMsg().isSetField(6064)){
                double pRate = Double.parseDouble( this.getClientOrder().getNewOrderRequestMsg().getString(6064));
                this.getClientOrder().setParticipationRate(pRate/100.0);
            }
        }catch (Exception ex){
            //LogFactory.error("Invalid participatio rate",ex);
        }

        try{
            SecurityExchange exDest =this.getClientOrder().getNewOrderRequestMsg().getSecurityExchange();
            if(exDest.getValue().equalsIgnoreCase("SZ")){
                this.getClientOrder().setExchangeDest("sz");
            }else{
                this.getClientOrder().setExchangeDest("sh");
            }
        }catch (Exception ex){
            //LogFactory.error("Invalid exDest",ex);
        }

        //set Symbol
        try{
            Symbol symbol = new Symbol();
            this.getClientOrder().getNewOrderRequestMsg().get(symbol);
            this.getClientOrder().setSymbol(symbol.getValue());
        }catch (FieldNotFound ex){
            Alert.fireAlert(Alert.Severity.Major,String.format(Alert.FIELD_NOT_FOUND_KEY,"Symbol",this.getID()),
                    this.getClientOrder().getNewOrderRequestMsg().toString(),ex);
        }

        try{
            String securityType =this.getClientOrder().getNewOrderRequestMsg().getString(167);
            this.getClientOrder().setSecurityType(securityType);

        }catch (Exception ex){
            //LogFactory.error("Invalid securityType",ex);
        }

        //set start/end time
        try{
            if(this.getClientOrder().getNewOrderRequestMsg().isSetField(6062)){
                String  startTimeStr = this.getClientOrder().getNewOrderRequestMsg().getString(6062);
                LocalDateTime startTime = DateTimeUtil.getDateTime5(startTimeStr);
                this.getClientOrder().setEffectiveTime(startTime);
                this.transactionTime = startTime.plusSeconds(5);
            }else{
                this.getClientOrder().setEffectiveTime(LocalDateTime.now().plusSeconds(5));
            }

            if(this.getClientOrder().getNewOrderRequestMsg().isSetField(6063)){
                String  endTimeStr = this.getClientOrder().getNewOrderRequestMsg().getString(6063);
                LocalDateTime endTime = DateTimeUtil.getDateTime5(endTimeStr );
                this.getClientOrder().setExpireTime(endTime);
            }else{
                this.getClientOrder().setExpireTime(LocalDateTime.now().plusMinutes(10));
            }

        }catch (Exception ex){
            //LogFactory.error("Parse time error when initialize order",ex);
        }


        this.getClientOrder().setOrdStatus(new OrdStatus(OrdStatus.PENDING_NEW));

        //if conditional order, subscribe market data
        //judge if this is a conditional order
        if(this.getClientOrder().getNewOrderRequestMsg().isSetField(Condition.TAG_REFER_SECURITY)){
            try{
                this.condition = Condition.build(this.getClientOrder().getNewOrderRequestMsg(),this);
                this.conditionalOrder=true;
            }catch (Exception ex){
                LogFactory.warn("Failed to initialize condition!");
            }
        }else{
            this.conditionalOrder=false;
        }

        //set issue type
        IssueType issueType  = IssueTypeDataManager.getInstance().IssueTypeHashMap().get(this.getClientOrder().getSecurityId());
        if(issueType!=null){
            this.getClientOrder().setStockName(issueType.getStockName());
        }

        this.pegConfiguration = PegConfiguration.build(this.getClientOrder().getNewOrderRequestMsg());

        //initialize decision configuration list
        TradingRuleProvider.getInstance().getConfigCache().keySet().forEach(className->{
            try{
                ArrayList<IDecisionConfig> configList =  TradingRuleProvider.getInstance().getConfigCache()
                        .get(className);
                configList.forEach(c->{
                    if(c.evaluate(this)==true){
                        decisionConfigHashMap.put(className,c);
                    }
                });
            }catch (Exception ex){
                LogFactory.error("Initialize decision config error",ex);
            }
        });
    }

    //find out which exchange orders to create/cancel based on current allocations
    public void assignNewCancelExchangeOrders(List<Allocation> exchangeOrderToCreate,List<ExchangeOrder> exchangeOrderToCancel,
                                              ArrayList<ExchangeOrder> exchangeOrdersExisted,
                                              ArrayList<Allocation> allocations,ArrayList<String> logLines){

        //check if client order is in pending cancel status
        if(this.getClientOrder().getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL){
            logLines.add("Current client order is in pending cancel, no new exchange order to create.");
            return;
        }

        //check if there is pending cancel exchange orders
        if(this.hasPendingCancelExchangeOrders()==true){
            logLines.add("There are some exchange orders in pending cancel, no new exchange order to create.");
            return;
        }

        List<ExchangeOrder> matchedExchangeOrders= exchangeOrdersExisted.stream()
                .filter(x -> FixUtil.IsOrderCompleted(x.getOrdStatus()) == false)
                .filter(x -> allocations.stream().
                        anyMatch(a -> a.getAllocatedPrice() == x.getPrice()
                                && a.getAllocatedQuantity() == x.getOrderQty()))
                .collect(Collectors.toList());

        List<String> matchedExchangeOrderIds = matchedExchangeOrders.stream().map(x -> x.getClientOrderId()).collect(Collectors.toList());
        List<ExchangeOrder> exchangeOrderToCancel_local =exchangeOrdersExisted.stream()
                .filter(x -> FixUtil.IsOrderCompleted(x.getOrdStatus()) == false && matchedExchangeOrderIds.contains(x.getClientOrderId()) == false)
                        //.filter(x->FixUtil.IsOrderCompleted(x.getOrdStatus())==false)
                .collect(Collectors.toList());
        exchangeOrderToCancel_local.forEach(x -> {
            exchangeOrderToCancel.add(x);
        });

        List<Allocation> exchangeOrderToCreate_local = allocations.stream().
                filter(x -> matchedExchangeOrders.stream().anyMatch(a -> a.getOrderQty() == x.getAllocatedQuantity() && a.getPrice() == x.getAllocatedPrice()) == false)
                .collect(Collectors.toList());
        exchangeOrderToCreate_local.forEach(x -> {
            exchangeOrderToCreate.add(x);
        });
    }

    public void process() throws Exception {
        //Todo: should use unallocated qty to tell if should split and sent out
        ArrayList<Allocation> allocations = new ArrayList<>();
        ArrayList<OmEvent> events = new ArrayList<>();
        ArrayList<String> logLines=new ArrayList<>();

        logLines.add("========**************============ Begin processing @ " + LocalTime.now() + " ========**************============");

        if(this.getClientOrder().getLeavesQty()==0.0){
            logLines.add("Zero quantity to allocate! @ "+this.getClientOrder().getFixStatusDisplay());
            logLines.add("========**************============ End processing @ "+lastProcessedTime+" ========**************============\r\n\r\n");
            //flush log
            LogFactory.logOrder(this.getClientOrder().getClientOrderId(), logLines);
            return ;
        }

        if(this.getClientOrder().getLeavesQty()<0.0){
            logLines.add("Negative quantity to allocate! @ "+this.getClientOrder().getFixStatusDisplay());
            logLines.add("========**************============ End processing @ "+lastProcessedTime+" ========**************============\r\n\r\n");
            //flush log
            LogFactory.logOrder(this.getClientOrder().getClientOrderId(), logLines);
            return ;
        }

        this.decisionChain.invokeDecision(this, allocations, events, logLines);

        if(allocations.size()==0){
            logLines.add("no allocation created ...");
        }else{
            ArrayList<Allocation> exchangeOrderToCreate = new ArrayList<>();
            ArrayList<ExchangeOrder> exchangeOrderToCancel = new ArrayList<>();

            this.assignNewCancelExchangeOrders(exchangeOrderToCreate, exchangeOrderToCancel,
                    this.exchangeOrders, allocations, logLines);

            logLines.add("The existed exchange orders ...");

            logLines.add(ClientOrder.toString(this.getExchangeOrders()));

            if (exchangeOrderToCancel.size() > 0) {

                logLines.add("Cancel the following exchange orders ...");
                logLines.add(ClientOrder.toString(exchangeOrderToCancel));

                exchangeOrderToCancel.forEach(x -> {
                    try {
                        if (x.getOrdStatus().getValue() == OrdStatus.PENDING_CANCEL) {
                            logLines.add("Exg order " + x.getClientOrderId() + " in in pending cancel, can't cancel it again");
                        } else if (FixUtil.IsOrderCompleted(x.getOrdStatus()) == true) {
                            logLines.add("Exg order " + x.getClientOrderId() + " is " + x.getFixStatusDisplay() + ", can't cancel it");
                        } else {
                            FixMsgHelper.cancelExchangeOrder(x);
                            x.setOrdStatus(new OrdStatus(OrdStatus.PENDING_CANCEL));
                        }
                    } catch (Exception ex) {
                        Alert.fireAlert(Alert.Severity.Major, String.format(
                                Alert.SENDING_MSG_ERROR, x.getClientOrderId()), "Cancel exchange order error!@" + ex.getMessage(), ex);
                    }
                });

            }else if(exchangeOrderToCreate.size()>0){
                logLines.add("Generate the following exchange orders ...");
                ArrayList<ExchangeOrder> exchangeOrdersToGenerate = new ArrayList<>();
                for(Allocation allocation:exchangeOrderToCreate){
                    SessionID sessionID = getSessionPoolPicker().pickUpSession(); //SessionPool.getInstance().pickupExchangeSessionID(this);
                    ExchangeOrder exchangeOrder = new ExchangeOrder(this.getClientOrder(),sessionID,
                            allocation.getAllocatedQuantity(),allocation.getAllocatedPrice(),allocation.getDecisionType(),allocation.getCategory());
                    try{
                        FixMsgHelper.sendOutExchangeOrder(exchangeOrder);
                        OrderPool.getExchangeOrderMap().putIfAbsent(exchangeOrder.getClientOrderId(), exchangeOrder);
                        exchangeOrdersToGenerate.add(exchangeOrder);
                    }catch (Exception ex){
                        Alert.fireAlert(Alert.Severity.Major,String.format(
                                Alert.SENDING_MSG_ERROR, exchangeOrder.getClientOrderId()),"Send out new exchange order error!@"+ex.getMessage(),ex);
                    }
                }

                this.getExchangeOrders().addAll(exchangeOrdersToGenerate);
                //logLines.add(ClientOrder.printOut(exchangeOrdersToGenerate));
                logLines.add(ClientOrder.toString(exchangeOrdersToGenerate));
            }
        }

        Object[] nonWakeUpEvents= events.stream().filter(x -> x.getEventType() != EventType.WAKEUP)
                .toArray();
        if(nonWakeUpEvents!=null && nonWakeUpEvents.length>0){
            for (Object event:nonWakeUpEvents){
                OmEvent omEvent = (OmEvent)event;
                controller.enqueueEvent(omEvent.getEventType(),omEvent.getDataHandler(),omEvent.getTriggerData());
            }
        }

        Optional<OmEvent> latestWakeUpEvent = events.stream().filter(x -> x.getEventType() == EventType.WAKEUP)
                .findAny();
        if(latestWakeUpEvent.isPresent()){
            controller.enqueueEvent(EventType.WAKEUP,latestWakeUpEvent.get().getDataHandler(),null);
        }

        lastProcessedTime = LocalDateTime.now();

        logLines.add("========**************============ End processing @ "+lastProcessedTime+" ========**************============\r\n\r\n");

        //flush log
        publishMsg(false);

        if(this.isReportProgressNeeded()==true){
            LogFactory.logOrder(this.getClientOrder().getClientOrderId(),logLines);
        }
    }

    public boolean isConditionalOrder() {
        return conditionalOrder;
    }

    public Condition getCondition() {
        return condition;
    }

    //region pause/reusme section
    private boolean pauseByUser;
    private boolean resumeByUser;

    public boolean isPauseByUser() {
        return pauseByUser;
    }
    public boolean isResumeByUser() {
        return resumeByUser;
    }

    public void pause(boolean force){
        if(FixUtil.IsClientOrderCompleted(this)==true){
            //can't pause a completed order
            LogFactory.warn("Can't pause a completed order:" + this.getClientOrder().getClientOrderId());
            return;
        }

        ClientOrder order = this.getClientOrder();
        if(order.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL){
            LogFactory.warn("Can't pause a pending cancel order:" + this.getClientOrder().getClientOrderId());
            return;
        }

        if(force==false){
            if(order.getPauseResumeState()==OrderState.PENDING_PAUSE || order.getPauseResumeState()==OrderState.PAUSED){
                LogFactory.warn("Can't pause a pending pause or paused order:" + this.getClientOrder().getClientOrderId() + " whose current pause/resume state is" + order.getPauseResumeState());
                return;
            }
        }

        if (force == true) {
            pauseByUser = true;
            resumeByUser = false;
            Alert.fireAlert(Alert.Severity.Info, String.format(Alert.PAUSE_ORDER_INFO, this.getAlertID()), "Pause by user!", null);
        }

        //Alert.clearAlert(String.format(Alert.RESUME_ORDER_INFO, this.getClientOrder().getClientOrderId()));

        //send pause request to exchange orders;
        if(this.getExchangeOrders()!=null && this.getExchangeOrders().size()>0){
            this.getExchangeOrders().stream().forEach(exOrder-> {
                if (FixUtil.IsOrderCompleted(exOrder.getOrdStatus()) == false &&
                        exOrder.getOrdStatus().getValue() != OrdStatus.PENDING_CANCEL) {
                    try {
                        ClOrdID clOrdID = new ClOrdID(exOrder.getClientOrderId());
                        OrderPauseResumeRequest orderPauseResumeRequest = new OrderPauseResumeRequest(clOrdID,
                                new StrategyStatusType(StrategyStatusType.Paused));
                        exOrder.setPauseResumeState(OrderState.PENDING_PAUSE);
                        FixMsgHelper.sendMessage(orderPauseResumeRequest, exOrder.getSessionID(), FixMsgHelper.EXG_OUT_PAUSE_ORDER, order.getClientOrderId());
                    } catch (Exception ex) {
                        LogFactory.error("Sending out pause request error!", ex);
                    }
                }
            });
        }


        order.setPauseResumeState(OrderState.PENDING_PAUSE);

        //Alert.clearAlert(String.format(Alert.RESUME_ORDER_INFO, this.getClientOrder().getClientOrderId()));

        Alert.fireAlert(Alert.Severity.Info,
                String.format(Alert.PAUSE_ORDER_INFO, this.getAlertID()),
                String.format("Pause condition %s triggered!", this.getCondition()==null?"":this.getCondition().toString()), null);
    }

    public void resume(boolean force){
        if(FixUtil.IsClientOrderCompleted(this)==true){
            //can't pause a completed order
            LogFactory.warn("Can't resume a completed order:" + this.getClientOrder().getClientOrderId());
            return;
        }

        ClientOrder order = this.getClientOrder();
        if(order.getOrdStatus().getValue()==OrdStatus.PENDING_CANCEL){
            LogFactory.warn("Can't resume a pending cancel order:" + this.getClientOrder().getClientOrderId());
            return;
        }

        if(force==true){
            pauseByUser=false;
            resumeByUser=true;
            Alert.fireAlert(Alert.Severity.Info, String.format(Alert.RESUME_ORDER_INFO, this.getClientOrder().getClientOrderId()), "Resume by user!", null);
        }

        //Alert.clearAlert(String.format(Alert.PAUSE_ORDER_INFO, this.getClientOrder().getClientOrderId()));

        if(order.getPauseResumeState()!=OrderState.PENDING_PAUSE && order.getPauseResumeState()!=OrderState.PAUSED){
            LogFactory.warn("Can't resume a pending resume or resumed order which is not in paused/pending pause state:" + order.getClientOrderId()
                    + ", whose current pause_resume state is " + order.getPauseResumeState().toString());
            return;
        }

        //send resume request to exchange orders;
        if(this.getExchangeOrders()!=null && this.getExchangeOrders().size()>0){
            this.getExchangeOrders().stream().forEach(exOrder->{
                if(FixUtil.IsOrderCompleted(exOrder.getOrdStatus())==false &&
                        exOrder.getOrdStatus().getValue()!=OrdStatus.PENDING_CANCEL){
                    try{
                        ClOrdID clOrdID = new ClOrdID(exOrder.getClientOrderId());
                        OrderPauseResumeRequest orderPauseResumeRequest = new OrderPauseResumeRequest(clOrdID,
                                new StrategyStatusType(StrategyStatusType.Running));
                        exOrder.setPauseResumeState(OrderState.PENDING_RESUME);
                        FixMsgHelper.sendMessage(orderPauseResumeRequest,exOrder.getSessionID(),FixMsgHelper.EXG_OUT_RESUME_ORDER,order.getClientOrderId());
                    }catch (Exception ex){
                        LogFactory.error("Sending out resume request error!",ex);
                    }
                }
            });
        }

        order.setPauseResumeState(OrderState.PENDING_RESUME);
        //Alert.clearAlert(String.format(Alert.PAUSE_ORDER_INFO, this.getClientOrder().getClientOrderId()));

        //Todo: use alert to verify pause_resume feature
        Alert.fireAlert(Alert.Severity.Info,
                String.format(Alert.RESUME_ORDER_INFO, this.getAlertID()),
                //String.format("Resume condition %s triggered!", this.getCondition().toString()), null);
                String.format("Resume condition %s triggered!", this.getCondition()==null?"":this.getCondition().toString()), null);
    }
    //endregion

    public LocalDateTime getLastProcessedTime() {
        return lastProcessedTime;
    }

    private double getOpenQty(){
        if(this.getExchangeOrders()==null || this.getExchangeOrders().size()==0)
            return 0;
        return this.getExchangeOrders().stream().mapToDouble(x->x.getLeavesQty())
                .sum();
    }

    private double getAvgPrice(){
        if(this.getExchangeOrders()==null || this.getExchangeOrders().size()==0)
            return 0;
        double totalAmt= this.getExchangeOrders().stream().mapToDouble(x->x.getAvgPrice()*x.getCumQty())
                .sum();
        double totalQty = this.getExchangeOrders().stream().mapToDouble(x->x.getCumQty())
                .sum();
        return totalQty>0? totalAmt/totalQty :0;
    }

    //only directly called by test case
    public void publishMsg(Boolean force ){

        if(this.isReportProgressNeeded()==false){
            return;
        }

        ClientOrder order = getClientOrder();
        try{
            // publish new order
            HashMap<String, String> msgDic = new HashMap();
            msgDic.put("OrderID", order.getClientOrderId());
            msgDic.put("Side", order.getOrderSide().getValue() ==Side.BUY?"Buy":"Sell");
            msgDic.put("EffectiveTime", order.getEffectiveTime().toString());
            msgDic.put("ExpireTime", order.getExpireTime().toString());
            msgDic.put("OrdQty", Long.toString(order.getOrderQty()) );
            msgDic.put("CumQty", Long.toString(order.getCumQty()) );
            msgDic.put("LeavesQty", Double.toString(order.getLeavesQty()));
            msgDic.put("OpenQty", Double.toString(getOpenQty()));
            msgDic.put("AvgPrice", Double.toString(getAvgPrice()));
            msgDic.put("Type", order.getOrderType());
            msgDic.put("Price", order.getOrderType().equals("MARKET") ? "MARKET" : Double.toString(order.getPrice()));
            msgDic.put("Symbol", order.getSymbol());
            msgDic.put("MdSymbol", order.getSecurityId());
            msgDic.put("ExDestination", order.getExchangeDest());
            msgDic.put("OrdStatus", order.getFixStatusDisplay());
            msgDic.put("OrderState", order.getOrderStateDisplay());
            msgDic.put("Algo", this.isPeggingOrder()?"PEGGING":"ALGO");
            msgDic.put("SecondaryClOrd", order.getSecondaryCloId());
            msgDic.put("ClientID", order.getClientId());
            msgDic.put("OpenClose", "N/A");
            msgDic.put("ParticipationRate", Double.toString(order.getParticipationRate()) );
            if (!StringUtils.isEmpty(order.getAccountId()))
            {
                msgDic.put("AccountId", order.getAccountId());
            }
            msgDic.put("MarginType", "N/A");

            msgDic.put("ADV20", Long.toString(order.getAdv20()));
            msgDic.put("MDV21", Long.toString(order.getMdv21()));

            if(Double.isNaN(order.getAdv20())==false && order.getAdv20()>0){
                double pct = ((double)order.getOrderQty())/order.getAdv20();
                msgDic.put("PctADV20",String.format("%.2f%%",pct*100.0));
            }else{
                msgDic.put("PctADV20", "N/A");
            }

            msgDic.put("StockName", order.getStockName());
            msgDic.put("SecurityType", order.getSecurityType());

            msgDic.put("Instance", String.format("%s:%s", GlobalConfig.getMonitorIP(),GlobalConfig.getMonitorPort()) );

            double rcr = ((double)order.getCumQty()/(double)order.getOrderQty());
            msgDic.put("RCR", Double.toString(rcr * 100.0));
            msgDic.put("TCR", "-1.0");
            msgDic.put("Lag", "-1.0");

            StringBuffer strBuilder = new StringBuffer();
            exchangeOrders.forEach(exg -> {
                strBuilder.append(String.format("%f@%f|",exg.getLeavesQty(),exg.getPrice()));
            });

            msgDic.put("openQtyPrice", strBuilder.toString().replace("|$",""));

            StringBuffer sb = new StringBuffer();
            this.getExchangeOrders().forEach(x -> {
                sb.append(String.format("%s,", x.getClientOrderId()));
            });
            msgDic.put("exchangeSliceIDs", sb.toString());

            if(getIntervalMarketData()!=null){
                msgDic.put("iVWP", Double.toString(getIntervalMarketData().getVwp()));
                msgDic.put("iVWPVS", Double.toString(getIntervalMarketData().getVwpvs()));

                if(order.getCumQty()>0 && getIntervalMarketData().getVwp()>0){
                    double slipage = order.getOrderSide().getValue() == Side.SELL?
                            (getAvgPrice() - getIntervalMarketData().getVwp()) / getIntervalMarketData().getVwp() :
                            (getIntervalMarketData().getVwp() - getAvgPrice()) / getIntervalMarketData().getVwp();
                    slipage *= 10000;
                    msgDic.put("SlipageInBps", Double.toString(slipage));
                }else{
                    msgDic.put("SlipageInBps", "0.0");
                }

                if(getIntervalMarketData().getVwp()>0){
                    double actualPov = ((double)order.getCumQty())/ getIntervalMarketData().getVwp();
                    msgDic.put("ActualPOV",Double.toString(actualPov));
                }else{
                    msgDic.put("ActualPOV",Double.toString(0.0));
                }
            }

            if(getAllDayIntervalMarketData()!=null){
                msgDic.put("VWP", Double.toString(getAllDayIntervalMarketData().getVwp()));
                msgDic.put("VWPVS", Double.toString(getAllDayIntervalMarketData().getVwpvs()));
            }

            if(getRealTimeMarketData()!=null){
                msgDic.put("Trdst", getRealTimeMarketData().getTradeStatus().toString());
                msgDic.put("rtmd",String.format("lu=%f,ld=%f",getRealTimeMarketData().getLu(),
                        getRealTimeMarketData().getLd()));
            }

            StringBuffer buffer = new StringBuffer();
            this.getExchangeOrders().forEach(x -> {
                buffer.append(String.format("%s,", x.getClientOrderId()));
            });

            String allExchSlicesTopic = "ORD.ALLEXCH." + getClientOrder().getClientOrderId();
            HashMap<String,String> exchData = new HashMap<>();
            exchData.put("IDs", buffer.toString());
            TradeDataMqManager.getInstance().sendMsg(allExchSlicesTopic, exchData, force, "");

            msgDic.put("AllExchSliceTopic", allExchSlicesTopic);
            TradeDataMqManager.getInstance().sendMsg("ORD.CLIENT." + order.getClientOrderId(), msgDic, force, "");

            this.getExchangeOrders().forEach(x->{
                try {
                    x.reportProgress();
                }catch (Exception ex){
                    LogFactory.error("exchange order report prgress error!",ex);
                }
            });
        }catch (Exception ex){
            Alert.fireAlert(Alert.Severity.Critical,"ORD.CLIENT." + order.getClientOrderId(),"publish msg error",ex);
        }

    }

    public RealTimeMarketData getRealTimeMarketData() {
        return realTimeMarketData;
    }

    public void setRealTimeMarketData(RealTimeMarketData realTimeMarketData) {
        this.realTimeMarketData = realTimeMarketData;
    }

    public IntervalMarketData getIntervalMarketData() {
        return intervalMarketData;
    }

    public void setIntervalMarketData(IntervalMarketData intervalMarketData) {
        this.intervalMarketData = intervalMarketData;
    }

    public AllDayIntervalMarketData getAllDayIntervalMarketData() {
        return allDayIntervalMarketData;
    }

    public void setAllDayIntervalMarketData(AllDayIntervalMarketData allDayIntervalMarketData) {
        this.allDayIntervalMarketData = allDayIntervalMarketData;
    }

    //set flag as if the hander should report current progress of execution report
    private boolean reportProgressNeeded=false;
    public boolean isReportProgressNeeded() {
        return reportProgressNeeded;
    }
    public void setReportProgressNeeded(boolean reportProgressNeeded) {
        this.reportProgressNeeded = reportProgressNeeded;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

}
