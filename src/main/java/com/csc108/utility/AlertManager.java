package com.csc108.utility;

import com.csc108.configuration.GlobalConfig;
import com.csc108.infrastructure.pooledActiveMQ.ConnectionFactoryProvider;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnection;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnectionFactory;
import com.csc108.log.LogFactory;
import com.csc108.model.cache.OrderbookDataManager;
import org.apache.activemq.ActiveMQConnectionFactory;
import quickfix.DataDictionary;

import javax.jms.*;
import java.util.HashMap;

/**
 * Created by zhangbaoshan on 2016/5/31.
 */
public class AlertManager {
    private AlertManager(){};
    private static AlertManager instance=new AlertManager();

    private ConnectionFactory connectionFactory ;
    private Connection connection;
    private Session session;
    private Topic topic ;
    private MessageProducer producer;

    public static AlertManager getInstance() {
        return instance;
    }

    public void init() throws Exception{
        try{
            //connectionFactory = new ActiveMQConnectionFactory(GlobalConfig.getAlertMqUrl());
            connectionFactory = ConnectionFactoryProvider.getConnectionFactory();
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException ex) {
                    LogFactory.error("Init connection to market avtive mq error", ex);
                }
            });

            connection.start();
            session=connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic(Alert.Topic);
            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        }catch (Exception ex){
            LogFactory.error("Initilze active mq to aler error!",ex);
            throw ex;
        }
    }

    public void stop(){
        try{
            if(connectionFactory instanceof PooledConnectionFactory)
                ((PooledConnectionFactory)connectionFactory).stop();
            else{
                if(session!=null) session.close();
                if(connection!=null) connection.close();
            }
        }catch (Exception ex){
            LogFactory.error("close active mq of alert error",ex);
        }
    }

    public void sendMsg(HashMap<String,String> msg){
        if(GlobalConfig.isActive_mq_available()==false)
            return;

        try{
//            Connection connection =  ConnectionFactoryProvider.getConnectionFactory().createConnection();
//
            //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //topic = session.createTopic(Alert.Topic);

            //MessageProducer producer = session.createProducer(topic);
            //producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            MapMessage msgMap=session.createMapMessage();

            msg.keySet().stream().forEach(x -> {
                try {
                    msgMap.setString(x, msg.get(x));
                } catch (Exception ex) {
                    LogFactory.error("map msg error!", ex);
                }
            });

            producer.send(msgMap);
            //session.close();
        }catch (Exception ex){
            LogFactory.error("send alert to mq error",ex);
        }
    }
}
