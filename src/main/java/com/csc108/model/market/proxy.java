package com.csc108.model.market;

import com.csc108.model.data.Security;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by zhangbaoshan on 2016/5/27.
 */
public class proxy {

    private List<Quote> bid;
    private double lastPrice;
    private Security security;
    private double turnover;
    private double doneVolume;
    private double preClose;
    private double openPrice;
    private double closePrice;
    private double highestPrice;
    private double lowestPrice;
    private LocalDateTime dateTime;

    public List<Quote> getBid() {
        return bid;
    }

    public void setBid(List<Quote> bid) {
        this.bid = bid;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public double getTurnover() {
        return turnover;
    }

    public void setTurnover(double turnover) {
        this.turnover = turnover;
    }

    public double getDoneVolume() {
        return doneVolume;
    }

    public void setDoneVolume(double doneVolume) {
        this.doneVolume = doneVolume;
    }

    public double getPreClose() {
        return preClose;
    }

    public void setPreClose(double preClose) {
        this.preClose = preClose;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
