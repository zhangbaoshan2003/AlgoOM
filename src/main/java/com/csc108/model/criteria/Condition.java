package com.csc108.model.criteria;

import com.csc108.model.cache.OrderbookDataManager;
import com.csc108.model.data.Security;
import com.csc108.model.data.SecurityType;
import com.csc108.model.fixModel.order.OrderHandler;
import com.csc108.model.market.OrderBook;
import com.csc108.model.market.Quote;
import quickfix.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LEGEN on 2016/6/4.
 */
public class Condition {
    public static final int TAG_REFER_SECURITY=7200;
    public static final int TAG_OP=7206;
    public static final int TAG_ACTION=7205;
    public static final int TAG_BENCHMARK=7201;
    public static final int TAG_ABS_VALUE=7202;
    public static final int TAG_RELATIVE_VALUE=7204;

    private final Operation operation;
    //private final String referenceSymbol;
    private final Benchmark benchmark;
    private final TradeAction tradeAction;
    private final TradeAction oppositeTradeAction;
    //private final Double benchMarkValue;
    private final double absValue;
    private final double relativeValue;
    private final boolean isAbsComparision;

    private final String referSecurity;
    public String getReferSecurity(){
        return referSecurity;
    }

    private OrderBook buildDummyOrderBook(String symbol) {
        List<Quote> ask = new ArrayList<Quote>();
        ask.add(new Quote(10.0, 1000));
        ask.add(new Quote(10.1, 2000));
        ask.add(new Quote(10.2, 3000));

        List<Quote> bid = new ArrayList<Quote>();
        bid.add(new Quote(9.9, 4000));
        bid.add(new Quote(9.8, 5000));
        bid.add(new Quote(9.7, 6000));

        OrderBook book = OrderBook.of();
        book.setAsk(ask);
        book.setBid(bid);

        book.setPreClose(10.0);
        book.setHighestPrice(11.0);
        book.setLastPrice(9.0);
        book.setOpenPrice(9.2);
        book.setSecurity(new Security(symbol, SecurityType.Stock));

        return book;
    }

    private OrderBook referenceOrderBook;

//    public String getReferenceSymbol(){
//        return this.referenceSymbol;
//    }

    private Condition(Operation op,//OrderBook referOrderBook,
                      String referSecurity,
                      Benchmark benchmark,TradeAction tradeAction, double absValue,double relativeValue){
        this.referSecurity = referSecurity;
        this.operation = op;
        //this.referenceSymbol = referOrderBook.getSecurity().getSymbol();
        this.benchmark = benchmark;

        this.tradeAction = tradeAction;

        if(Double.isNaN(absValue)){
            isAbsComparision=false;
            this.relativeValue = relativeValue;
            this.absValue= Double.NaN;
        }else{
            isAbsComparision=true;
            this.absValue= absValue;
            this.relativeValue = Double.NaN;
        }

//        double referValue =  getBenchmarkValue(referOrderBook,benchmark);
//
//        if(isAbsComparision==false){
//            this.benchMarkValue = referValue*(1+relativeValue/100.0);
//        }else{
//            this.benchMarkValue=this.absValue;
//        }

        if(tradeAction==TradeAction.Pause){
            oppositeTradeAction=TradeAction.Resume;
        }else{
            oppositeTradeAction=TradeAction.Pause;
        }
    }

    private double getBenchmarkValue(OrderBook orderBook,Benchmark benchmark){
        double benchMarkValue;
        if(benchmark==Benchmark.Ask1Px){
            benchMarkValue = orderBook.getAskPrice(0);
        }else if(benchmark==Benchmark.Bib1Px){
            benchMarkValue = orderBook.getBidPrice(0);
        }else if(benchmark==Benchmark.LasPx){
            benchMarkValue = orderBook.getLastPrice();
        }else if(benchmark==Benchmark.OpenPx){
            benchMarkValue = orderBook.getOpenPrice();
        }else if(benchmark==Benchmark.PreClose){
            benchMarkValue = orderBook.getPreClose();
        }else if(benchmark==Benchmark.OpenPx){
            benchMarkValue = orderBook.getOpenPrice();
        }else if(benchmark==Benchmark.TopClose){
            benchMarkValue = orderBook.getHighestPrice();
        }else if(benchmark==Benchmark.BottomClose){
            benchMarkValue = orderBook.getLowestPrice();
        }else{
            benchMarkValue = orderBook.getPreClose();
        }
        return benchMarkValue;
    }

    public TradeAction evaluate(){
        //Todo: dummy logic for evaluate condition
        OrderBook orderBook = OrderbookDataManager.getInstance().getLatestOrderBook(this.referSecurity);
        if(orderBook==null)
            throw new IllegalArgumentException("Orderbook ["+this.referSecurity +"] is not available to evaluate the conditional order!");

        if(Double.isNaN(orderBook.getLastPrice()))
            throw new IllegalArgumentException("Orderbook ["+this.referSecurity +"] has no last price to evaluate the conditional order!");
        double currentBenchmarkValue = getBenchmarkValue(orderBook,Benchmark.LasPx);

        double benchMarkValue = getBenchmarkValue(orderBook,this.benchmark);
        if(Double.isNaN(benchMarkValue))
            throw new IllegalArgumentException("Orderbook ["+this.referSecurity +"] has no benchmark value to evaluate the conditional order!");

        if(isAbsComparision==false){
            benchMarkValue = benchMarkValue*(1+relativeValue/100.0);
        }else{
            benchMarkValue=this.absValue;
        }

        if(operation==Operation.Equal){

            if(Double.compare(currentBenchmarkValue ,benchMarkValue)==0){
                return tradeAction;
            }else{
                return oppositeTradeAction;
            }
        }
        if(operation==Operation.Greater){
            if(currentBenchmarkValue > benchMarkValue){
                return tradeAction;
            }else{
                return oppositeTradeAction;
            }
        }
        if(operation==Operation.Less){
            if(currentBenchmarkValue < benchMarkValue){
                return tradeAction;
            }else{
                return oppositeTradeAction;
            }
        }
        if(operation==Operation.GreaterEqual){
            if(currentBenchmarkValue >= benchMarkValue){
                return tradeAction;
            }else{
                return oppositeTradeAction;
            }
        }
        if(operation==Operation.LessEqual){
            if(currentBenchmarkValue <= benchMarkValue){
                return tradeAction;
            }else{
                return oppositeTradeAction;
            }
        }

        return oppositeTradeAction;
    }

    public static Condition build (Message message,OrderHandler handler) throws Exception {
        String referSecurity="";
        Benchmark benchmark=Benchmark.PreClose;
        Operation op=Operation.Equal;
        TradeAction tr=TradeAction.Pause;
        OrderBook orderBook=null;
        double absValue=Double.NaN;
        double relativeValue = Double.NaN;

        if(message.isSetField(TAG_OP)){
            int opStr = message.getInt(TAG_OP);
            switch (opStr){
                case 0:
                    op = Operation.Greater;
                    break;
                case 1:
                    op = Operation.Less;
                    break;
                case 2:
                    op = Operation.GreaterEqual;
                    break;
                case 3:
                    op = Operation.LessEqual;
            }
        }else{
            throw new IllegalArgumentException("not defined operation tag "+TAG_OP);
        }

        if(message.isSetField(TAG_ACTION)){
            int opStr = message.getInt(TAG_ACTION);
            switch (opStr){
                case 0:
                    tr = TradeAction.Resume;
                    break;
                case 1:
                    tr = TradeAction.Pause;
                    break;
            }
        }else{
            throw new IllegalArgumentException("not defined trade action tag "+TAG_ACTION);
        }

        if(message.isSetField(TAG_BENCHMARK)){
            int opStr = message.getInt(TAG_BENCHMARK);
            switch (opStr){
                case 0:
                    benchmark = Benchmark.LasPx;
                    break;
                case 10:
                    benchmark = Benchmark.PreClose;
                    break;
                case 9:
                    benchmark = Benchmark.OpenPx;
                    break;
                case 8:
                    benchmark = Benchmark.BottomClose;
                    break;
                case 7:
                    benchmark = Benchmark.TopClose;
                    break;
            }
        }else{
            throw new IllegalArgumentException("not defined benchmark tag "+TAG_BENCHMARK);
        }

        if(message.isSetField(TAG_REFER_SECURITY)){
            String hsName= message.getString(TAG_REFER_SECURITY);
            referSecurity = OrderbookDataManager.getInstance().getSymbolByHsName(hsName);
            if(handler!=null){
                OrderbookDataManager.getInstance().subscribeOrderBook(referSecurity,false,handler);
            }
        }else{
            throw new IllegalArgumentException("not defined reference security "+TAG_REFER_SECURITY);
        }

        if(message.isSetField(TAG_ABS_VALUE)){
            absValue = message.getDouble(TAG_ABS_VALUE);
        }else if(message.isSetField(TAG_RELATIVE_VALUE)){
            relativeValue = message.getDouble(TAG_RELATIVE_VALUE);
        }else{
            throw new IllegalArgumentException("not defined relative nor absolute value ");
        }
        //return new Condition(op,orderBook,benchmark,tr,absValue,relativeValue);
        return new Condition(op,referSecurity,benchmark,tr,absValue,relativeValue);
    }

    @Override
    public String toString(){
        double lastPrice = Double.NaN;
        double benchMarkValue = Double.NaN;

        OrderBook orderBook = OrderbookDataManager.getInstance().getLatestOrderBook(this.referSecurity);
        if(orderBook==null)
            orderBook=buildDummyOrderBook("DummyOrderBook");

        lastPrice = getBenchmarkValue(orderBook,this.benchmark);
        benchMarkValue = getBenchmarkValue(orderBook,this.benchmark);
        if(isAbsComparision==false){
            benchMarkValue = benchMarkValue*(1+relativeValue/100.0);
        }else{
            benchMarkValue=this.absValue;
        }

        return String.format("When %s's %s(%f) %s then %s(%f), %s order",referSecurity,"last price",lastPrice,
                operation,benchmark,benchMarkValue,tradeAction);
    }
}
