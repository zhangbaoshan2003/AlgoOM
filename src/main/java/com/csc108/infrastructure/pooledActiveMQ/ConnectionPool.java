package com.csc108.infrastructure.pooledActiveMQ;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.transport.TransportListener;
import org.apache.commons.pool.ObjectPoolFactory;

/**
 * Created by LEGEN on 2016/6/18.
 */
public class ConnectionPool {
    private ActiveMQConnection connection;
    private Map cache;
    private AtomicBoolean started = new AtomicBoolean(false);
    private int referenceCount;
    private ObjectPoolFactory poolFactory;
    private long lastUsed = System.currentTimeMillis();
    private boolean hasFailed;
    private int idleTimeout = 30*1000;

    public ConnectionPool(ActiveMQConnection connection, ObjectPoolFactory poolFactory) {
        this(connection, new HashMap(), poolFactory);
        // Add a transport Listener so that we can notice if this connection should be expired due to
        // a connection failure.
        connection.addTransportListener(new TransportListener(){
            public void onCommand(Object command) {
            }
            public void onException(IOException error) {
                synchronized(ConnectionPool.this) {
                    hasFailed = true;
                }
            }
            public void transportInterupted() {
            }
            public void transportResumed() {
            }
        });
    }

    public ConnectionPool(ActiveMQConnection connection, Map cache, ObjectPoolFactory poolFactory) {
        this.connection = connection;
        this.cache = cache;
        this.poolFactory = poolFactory;
    }

    public void start() throws JMSException {
        if (started.compareAndSet(false, true)) {
            connection.start();
        }
    }

    synchronized public ActiveMQConnection getConnection() {
        return connection;
    }

    public Session createSession(boolean transacted, int ackMode) throws JMSException {
        SessionKey key = new SessionKey(transacted, ackMode);
        SessionPool pool = (SessionPool) cache.get(key);
        if (pool == null) {
            pool = createSessionPool(key);
            cache.put(key, pool);
        }
        PooledSession session = pool.borrowSession();
        return session;
    }

    synchronized public void close() {
        if( connection!=null ) {
            Iterator i = cache.values().iterator();
            while (i.hasNext()) {
                SessionPool pool = (SessionPool) i.next();
                i.remove();
                try {
                    pool.close();
                } catch (Exception e) {
                }
            }
            try {
                connection.close();
            } catch (Exception e) {
            }
            connection = null;
        }
    }

    synchronized public void incrementReferenceCount() {
        referenceCount++;
    }

    synchronized public void decrementReferenceCount() {
        referenceCount--;
        lastUsed = System.currentTimeMillis();
        if( referenceCount == 0 ) {
            expiredCheck();
        }
    }

    /**
     * @return true if this connection has expired.
     */
    synchronized public boolean expiredCheck() {
        if( connection == null )
            return true;
        long t = System.currentTimeMillis();
        if( hasFailed || idleTimeout> 0 && t > lastUsed+idleTimeout ) {
            if( referenceCount == 0 ) {
                close();
            }
            return true;
        }
        return false;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    protected SessionPool createSessionPool(SessionKey key) {
        return new SessionPool(this, key, poolFactory.createPool());
    }

}
