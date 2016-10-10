package com.csc108.model;

import java.time.LocalDateTime;

/**
 * Created by zhangbaoshan on 2016/6/2.
 */
public class WakeupEvaluationData implements IEvaluationData {
    private final long wakeupTime;

    public long getWakeupTime() {
        return wakeupTime;
    }

    public WakeupEvaluationData (long wakeupTimeMillseconds){
        this.wakeupTime = wakeupTimeMillseconds;
    }
}
