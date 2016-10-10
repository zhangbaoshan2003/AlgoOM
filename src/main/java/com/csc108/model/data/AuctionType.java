package com.csc108.model.data;

import lombok.Getter;

/**
 * Created by zhangbaoshan on 2016/5/9.
 */

public enum AuctionType {
    None(0), // not an auction
    AMAuction(1), PMAuction(2), CloseAuction(3), All(4);
    @Getter
    private int value;

    private AuctionType(int value_) {
        value = value_;
    }
}

