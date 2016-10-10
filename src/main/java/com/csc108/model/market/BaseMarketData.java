package com.csc108.model.market;

import com.csc108.model.IEvaluationData;
import com.csc108.model.data.AuctionType;
import com.csc108.model.data.TradeStatus;

import java.time.LocalDateTime;

/**
 * Created by zhangbaoshan on 2016/8/17.
 */
public abstract class BaseMarketData implements IEvaluationData {
    //600000.sh
    private String symbol;
    private double vwp;
    private double vwpvs;
    private double hp;
    private double lp;
    private double indauc;
    private double indaucs;
    private TradeStatus tradeStatus = TradeStatus.Unknown;
    private AuctionType auctionType = AuctionType.None;
    private double op;
    private double lu ;
    private double ld ;
    private double tp ;
    private LocalDateTime dateTime;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getVwp() {
        return vwp;
    }

    public void setVwp(double vwp) {
        this.vwp = vwp;
    }

    public double getVwpvs() {
        return vwpvs;
    }

    public void setVwpvs(double vwpvs) {
        this.vwpvs = vwpvs;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getLp() {
        return lp;
    }

    public void setLp(double lp) {
        this.lp = lp;
    }

    public double getIndauc() {
        return indauc;
    }

    public void setIndauc(double indauc) {
        this.indauc = indauc;
    }

    public double getIndaucs() {
        return indaucs;
    }

    public void setIndaucs(double indaucs) {
        this.indaucs = indaucs;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public double getOp() {
        return op;
    }

    public void setOp(double op) {
        this.op = op;
    }

    public double getLu() {
        return lu;
    }

    public void setLu(double lu) {
        this.lu = lu;
    }

    public double getLd() {
        return ld;
    }

    public void setLd(double ld) {
        this.ld = ld;
    }

    public double getTp() {
        return tp;
    }

    public void setTp(double tp) {
        this.tp = tp;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
