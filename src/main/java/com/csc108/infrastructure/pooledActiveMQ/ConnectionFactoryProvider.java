package com.csc108.infrastructure.pooledActiveMQ;

import com.csc108.configuration.GlobalConfig;
import com.csc108.log.LogFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by LEGEN on 2016/6/19.
 */
public  class ConnectionFactoryProvider {

    private static ConnectionFactory connectionFactory;
    private static ConnectionFactory pooled_connectionFactory;

    static {
        connectionFactory = new ActiveMQConnectionFactory();

        pooled_connectionFactory= new PooledConnectionFactory(new ActiveMQConnectionFactory(GlobalConfig.getAlertMqUrl()));
        ((PooledConnectionFactory)pooled_connectionFactory).setMaxConnections(GlobalConfig.getMaxNumOfActiveMqConnections());

        for(int i=0;i<GlobalConfig.getMaxNumOfActiveMqConnections();i++){
            try{
                Connection connection = pooled_connectionFactory.createConnection();
                connection.setExceptionListener(new ExceptionListener() {
                    @Override
                    public void onException(JMSException ex) {
                        LogFactory.error("Init connection to market avtive mq error", ex);
                    }
                });
                connection.start();
                Session session=connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            }catch (Exception ex){
                ex.printStackTrace();
                LogFactory.error("Initialize connection pool failed",ex);
            }
        }
    }

    public static ConnectionFactory getConnectionFactory() throws Exception {
        if(GlobalConfig.getMaxNumOfActiveMqConnections()>1)
            return pooled_connectionFactory;
        return connectionFactory;
    }
}
