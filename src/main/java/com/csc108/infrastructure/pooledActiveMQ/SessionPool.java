package com.csc108.infrastructure.pooledActiveMQ;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.util.JMSExceptionSupport;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.activemq.AlreadyClosedException;

import javax.jms.JMSException;

/**
 * Created by zhangbaoshan on 2016/6/14.
 */
public class SessionPool implements PoolableObjectFactory {
    private ConnectionPool connectionPool;
    private SessionKey key;
    private ObjectPool sessionPool;

    public SessionPool(ConnectionPool connectionPool, SessionKey key, ObjectPool sessionPool) {
        this.connectionPool = connectionPool;
        this.key = key;
        this.sessionPool = sessionPool;
        sessionPool.setFactory(this);
    }

    public void close() throws Exception {
        if (sessionPool != null) {
            sessionPool.close();
        }
        sessionPool = null;
    }

    public PooledSession borrowSession() throws JMSException {
        try {
            Object object = getSessionPool().borrowObject();
            return (PooledSession) object;
        }
        catch (JMSException e) {
            throw e;
        }
        catch (Exception e) {
            throw JMSExceptionSupport.create(e);
        }
    }

    public void returnSession(PooledSession session) throws JMSException {
        // lets check if we are already closed
        getConnection();
        try {
            getSessionPool().returnObject(session);
        }
        catch (Exception e) {
            throw JMSExceptionSupport.create("Failed to return session to pool: " + e, e);
        }
    }

    // PoolableObjectFactory methods
    // -------------------------------------------------------------------------
    public Object makeObject() throws Exception {
        return new PooledSession(createSession(), this);
    }

    public void destroyObject(Object o) throws Exception {

        PooledSession session = (PooledSession) o;
        session.getSession().close();
    }

    public boolean validateObject(Object o) {
        return true;
    }

    public void activateObject(Object o) throws Exception {
    }

    public void passivateObject(Object o) throws Exception {
    }

    // Implemention methods
    // -------------------------------------------------------------------------
    protected ObjectPool getSessionPool() throws AlreadyClosedException {
        if (sessionPool == null) {
            throw new AlreadyClosedException();
        }
        return sessionPool;
    }

    protected ActiveMQConnection getConnection() throws JMSException {
        return connectionPool.getConnection();
    }

    protected ActiveMQSession createSession() throws JMSException {
        return (ActiveMQSession) getConnection().createSession(key.isTransacted(), key.getAckMode());
    }


}
