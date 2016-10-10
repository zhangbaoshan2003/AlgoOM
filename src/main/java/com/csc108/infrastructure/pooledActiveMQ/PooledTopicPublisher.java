package com.csc108.infrastructure.pooledActiveMQ;

import org.apache.activemq.ActiveMQTopicPublisher;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

/**
 * Created by LEGEN on 2016/6/18.
 */
public class PooledTopicPublisher extends PooledProducer implements TopicPublisher {

    public PooledTopicPublisher(ActiveMQTopicPublisher messageProducer, Destination destination) throws JMSException {
        super(messageProducer, destination);
    }

    public Topic getTopic() throws JMSException {
        return getTopicPublisher().getTopic();
    }

    public void publish(Message message) throws JMSException {
        getTopicPublisher().publish(message);
    }

    public void publish(Message message, int i, int i1, long l) throws JMSException {
        getTopicPublisher().publish(message, i, i1, l);
    }

    public void publish(Topic topic, Message message) throws JMSException {
        getTopicPublisher().publish(topic, message);
    }

    public void publish(Topic topic, Message message, int i, int i1, long l) throws JMSException {
        getTopicPublisher().publish(topic, message, i, i1, l);
    }

    protected ActiveMQTopicPublisher getTopicPublisher() {
        return (ActiveMQTopicPublisher) getMessageProducer();
    }
}
