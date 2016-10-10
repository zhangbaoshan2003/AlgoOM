package com.csc108.infrastructure.pooledActiveMQ;

import org.apache.activemq.ActiveMQQueueSender;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

/**
 * Created by LEGEN on 2016/6/18.
 */
public class PooledQueueSender extends PooledProducer implements QueueSender {

    public PooledQueueSender(ActiveMQQueueSender messageProducer, Destination destination) throws JMSException {
        super(messageProducer, destination);
    }

    public void send(Queue queue, Message message, int i, int i1, long l) throws JMSException {
        getQueueSender().send(queue, message, i, i1, l);
    }

    public void send(Queue queue, Message message) throws JMSException {
        getQueueSender().send(queue, message);
    }

    public Queue getQueue() throws JMSException {
        return getQueueSender().getQueue();
    }


    protected ActiveMQQueueSender getQueueSender() {
        return (ActiveMQQueueSender) getMessageProducer();
    }

}
