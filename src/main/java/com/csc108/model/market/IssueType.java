package com.csc108.model.market;

/**
 * Created by zhangbaoshan on 2016/8/30.
 */
public class IssueType {
    private final String exchange;
    private final String symbol;
    private final String stockId;
    private final String sType;
    private final boolean isTradable;
    private final boolean status;
    private final String stockName;

    public IssueType(String exchange_,String symbol_,String stockId_,String sType_,
                     boolean isTradable_,boolean status_,String stockName_){
        exchange=exchange_;
        symbol=symbol_;
        stockId=stockId_;
        sType = sType_;
        isTradable=isTradable_;
        status = status_;
        stockName=stockName_;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStockId() {
        return stockId;
    }

    public String getsType() {
        return sType;
    }

    public Boolean getIsTradable() {
        return isTradable;
    }

    public boolean getStatus() {
        return status;
    }

    public String getStockName() {
        return stockName;
    }
}
