package com.csc108.model.fix.sessionPool;

import quickfix.SessionID;

/**
 * Created by zhangbaoshan on 2016/11/24.
 */
public interface ISessionPoolPicker {
    public SessionID pickUpSession();
}
