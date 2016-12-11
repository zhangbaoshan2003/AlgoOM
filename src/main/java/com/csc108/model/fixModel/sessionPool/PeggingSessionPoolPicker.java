package com.csc108.model.fixModel.sessionPool;

import quickfix.SessionID;

/**
 * Created by zhangbaoshan on 2016/11/24.
 */
public class PeggingSessionPoolPicker implements ISessionPoolPicker {

    @Override
    public SessionID pickUpSession() {
        return SessionPool.getInstance().pickupPeggingExchangeSessionID();
    }
}
