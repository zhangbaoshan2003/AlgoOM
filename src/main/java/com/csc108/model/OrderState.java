package com.csc108.model;

/**
 * Created by zhangbaoshan on 2016/4/28.
 */
public enum  OrderState {
    INITIALIZED(0),SENT_TO_EXCHANGE(1),COMPLETED(2),UNKNOWN(3),
    PENDING_PAUSE(4),PAUSED(5),PAUSE_REJECTED(8),
    PENDING_RESUME(6),RESUMED(7),RESUME_REJECTED(9);

    private int value;

    private OrderState(int value_) {
        value = value_;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
