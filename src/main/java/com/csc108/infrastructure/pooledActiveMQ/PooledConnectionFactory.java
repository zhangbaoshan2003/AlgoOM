package com.csc108.infrastructure.pooledActiveMQ;

import java.util.*;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.Service;
import org.apache.activemq.util.IOExceptionSupport;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.ObjectPoolFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
/**
 * Created by LEGEN on 2016/6/18.
 */
public class PooledConnectionFactory implements ConnectionFactory, Service {
    private ConnectionFactory connectionFactory;
    private Map cache = new HashMap();
    private ObjectPoolFactory poolFactory;
    private int maximumActive = 500;
    private int maxConnections = 1;

    public PooledConnectionFactory() {
        this(new ActiveMQConnectionFactory());
    }

    public PooledConnectionFactory(String brokerURL) {
        this(new ActiveMQConnectionFactory(brokerURL));
    }

    public PooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connection createConnection() throws JMSException {
        return createConnection(null, null);
    }

    public synchronized Connection createConnection(String userName, String password) throws JMSException {
        ConnectionKey key = new ConnectionKey(userName, password);
        LinkedList pools = (LinkedList) cache.get(key);

        if (pools ==  null) {
            pools = new LinkedList();
            cache.put(key, pools);
        }

        ConnectionPool connection = null;
        if (pools.size() == maxConnections) {
            connection = (ConnectionPool) pools.removeFirst();
        }

        // Now.. we might get a connection, but it might be that we need to
        // dump it..
        if( connection!=null && connection.expiredCheck() ) {
            connection=null;
        }

        if (connection == null) {
            ActiveMQConnection delegate = createConnection(key);
            connection = createConnectionPool(delegate);
        }
        pools.add(connection);
        return new PooledConnection(connection);
    }

    protected ConnectionPool createConnectionPool(ActiveMQConnection connection) {
        return new ConnectionPool(connection, getPoolFactory());
    }

    protected ActiveMQConnection createConnection(ConnectionKey key) throws JMSException {
        if (key.getUserName() == null && key.getPassword() == null) {
            return (ActiveMQConnection) connectionFactory.createConnection();
        }
        else {
            return (ActiveMQConnection) connectionFactory.createConnection(key.getUserName(), key.getPassword());
        }
    }

    /**
     * @
     */
    public void start() {
        try {
            createConnection();
        }
        catch (JMSException e) {
            IOExceptionSupport.create(e);
        }
    }

    public void stop() throws Exception {
        for (Iterator iter = cache.values().iterator(); iter.hasNext();) {
            LinkedList linkedList = (LinkedList)iter.next();
            linkedList.stream().forEach(x -> {
                ConnectionPool connection = (ConnectionPool) x;
                connection.close();
            });

        }
    }

    public ObjectPoolFactory getPoolFactory() {
        if (poolFactory == null) {
            poolFactory = createPoolFactory();
        }
        return poolFactory;
    }

    /**
     * Sets the object pool factory used to create individual session pools for
     * each connection
     */
    public void setPoolFactory(ObjectPoolFactory poolFactory) {
        this.poolFactory = poolFactory;
    }

    public int getMaximumActive() {
        return maximumActive;
    }

    /**
     * Sets the maximum number of active sessions per connection
     */
    public void setMaximumActive(int maximumActive) {
        this.maximumActive = maximumActive;
    }

    /**
     * @return the maxConnections
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections the maxConnections to set
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    protected ObjectPoolFactory createPoolFactory() {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxIdle = maximumActive+1;
        config.maxActive = maximumActive;

        GenericObjectPoolFactory factory =new GenericObjectPoolFactory(null, config);
        return factory;
    }

}
