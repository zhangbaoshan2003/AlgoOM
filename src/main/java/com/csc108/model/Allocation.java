package com.csc108.model;

import com.csc108.model.fixModel.order.ExchangeOrder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by zhangbaoshan on 2016/5/1.
 */
public class Allocation implements Serializable {
    private final String ID= UUID.randomUUID().toString();
    private static final long serialVersionUID = -2174356994537890017L;
    private AllocationCategory category;
    private AllocationDecisionType decisionType;
    private ExchangeOrder matchedExchangeOrder=null;

    private LocalDateTime allocateTime=LocalDateTime.MAX;
    private double allocatedQuantity;
    private double allocatedPrice;

    public String getID() {
        return ID;
    }

    public double getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(double allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
    }

    public double getAllocatedPrice() {
        return allocatedPrice;
    }

    public void setAllocatedPrice(double allocatedPrice) {
        this.allocatedPrice = allocatedPrice;
    }

    public LocalDateTime getAllocateTime() {
        return allocateTime;
    }

    public void setAllocateTime(LocalDateTime allocateTime) {
        this.allocateTime = allocateTime;
    }

    public AllocationCategory getCategory() {
        return category;
    }

    public void setCategory(AllocationCategory category) {
        this.category = category;
    }

    public AllocationDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(AllocationDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public ExchangeOrder getMatchedExchangeOrder() {
        return matchedExchangeOrder;
    }

    public void setMatchedExchangeOrder(ExchangeOrder matchedExchangeOrder) {
        this.matchedExchangeOrder = matchedExchangeOrder;
    }
}


