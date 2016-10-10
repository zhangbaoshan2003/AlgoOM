package com.csc108.infrastructure.pooledActiveMQ;

/**
 * Created by zhangbaoshan on 2016/6/14.
 */
public class SessionKey {
    private boolean transacted;
    private int ackMode;
    private int hash;

    public SessionKey(boolean transacted, int ackMode) {
        this.transacted = transacted;
        this.ackMode = ackMode;
        hash = ackMode;
        if (transacted) {
            hash = 31 * hash + 1;
        }
    }

    public int hashCode() {
        return hash;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof SessionKey) {
            return equals((SessionKey) that);
        }
        return false;
    }

    public boolean equals(SessionKey that) {
        return this.transacted == that.transacted && this.ackMode == that.ackMode;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public int getAckMode() {
        return ackMode;
    }
}
