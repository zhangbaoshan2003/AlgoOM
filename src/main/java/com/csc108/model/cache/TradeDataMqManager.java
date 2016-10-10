package com.csc108.model.cache;

import com.csc108.configuration.GlobalConfig;
import com.csc108.infrastructure.pooledActiveMQ.ConnectionFactoryProvider;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnection;
import com.csc108.infrastructure.pooledActiveMQ.PooledConnectionFactory;
import com.csc108.infrastructure.pooledActiveMQ.PooledSession;
import com.csc108.log.LogFactory;
import com.csc108.utility.Alert;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangbaoshan on 2016/8/15.
 */
public class TradeDataMqManager {
    private PooledConnectionFactory connectionFactory ;
    private PooledConnection pooledConnection;
    private final int maxNumOfSessions = 200;
    private static TradeDataMqManager instance = new TradeDataMqManager();

    private TradeDataMqManager(){

    }

    public static TradeDataMqManager getInstance(){
        return instance;
    }

    public void initialize() throws Exception {
        try{
            connectionFactory = new PooledConnectionFactory(new ActiveMQConnectionFactory(GlobalConfig.getTradeDataMqUrl()));
            connectionFactory.setMaxConnections(1);
            connectionFactory.setMaximumActive(maxNumOfSessions);
            connectionFactory.start();

            pooledConnection=(PooledConnection)connectionFactory.createConnection();
            pooledConnection.start();

            //warm up sesion
            for (int i=0;i<maxNumOfSessions;i++){
                PooledSession session  = (PooledSession)pooledConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
                session.setIgnoreClose(true);
                session.close();
            }
        }catch (Exception ex){
            LogFactory.error("Initilze active mq to aler error!",ex);
            throw ex;
        }
    }

    public void stop(){
        try{
            connectionFactory.stop();
        }catch (Exception ex){
            LogFactory.error("Close trade data mq factory error!",ex);
        }
    }

    public void sendMsg(String topic, HashMap<String,String> msg,boolean forceToRefresh,String filter) throws Exception {
        if(GlobalConfig.isActive_mq_available()==false)
            return;

        try{
            PooledSession session =(PooledSession)pooledConnection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            MapMessage msgMap=session.createMapMessage();
            Topic sessionTopic = session.createTopic(topic);
            MessageProducer producer = session.createProducer(sessionTopic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            msg.keySet().stream().forEach(x -> {
                try {
                    msgMap.setString(x, msg.get(x));
                } catch (Exception ex) {
                    LogFactory.error("map msg error!", ex);
                }
            });

            producer.send(msgMap);

            session.close();
        }catch (Exception ex){
            LogFactory.error("send trade data to mq error",ex);
        }
    }

}
