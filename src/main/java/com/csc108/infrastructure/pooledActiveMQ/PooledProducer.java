package com.csc108.infrastructure.pooledActiveMQ;

import org.apache.activemq.ActiveMQMessageProducer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

/**
 * Created by zhangbaoshan on 2016/6/15.
 */
public class PooledProducer implements MessageProducer {
    private ActiveMQMessageProducer messageProducer;
    private Destination destination;
    private int deliveryMode;
    private boolean disableMessageID;
    private boolean disableMessageTimestamp;
    private int priority;
    private long timeToLive;

    public PooledProducer(ActiveMQMessageProducer messageProducer, Destination destination) throws JMSException {
        this.messageProducer = messageProducer;
        this.destination = destination;

        this.deliveryMode = messageProducer.getDeliveryMode();
        this.disableMessageID = messageProducer.getDisableMessageID();
        this.disableMessageTimestamp = messageProducer.getDisableMessageTimestamp();
        this.priority = messageProducer.getPriority();
        this.timeToLive = messageProducer.getTimeToLive();
    }

    public void close() throws JMSException {
    }

    public void send(Destination destination, Message message) throws JMSException {
        send(destination, message, getDeliveryMode(), getPriority(), getTimeToLive());
    }

    public void send(Message message) throws JMSException {
        send(destination, message, getDeliveryMode(), getPriority(), getTimeToLive());
    }

    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        send(destination, message, deliveryMode, priority, timeToLive);
    }

    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        if (destination == null) {
            destination = this.destination;
        }
        ActiveMQMessageProducer messageProducer = getMessageProducer();

        // just in case let only one thread send at once
        synchronized (messageProducer) {
            messageProducer.send(destination, message, deliveryMode, priority, timeToLive);
        }
    }

    public Destination getDestination() {
        return destination;
    }

    public int getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public boolean getDisableMessageID() {
        return disableMessageID;
    }

    public void setDisableMessageID(boolean disableMessageID) {
        this.disableMessageID = disableMessageID;
    }

    public boolean getDisableMessageTimestamp() {
        return disableMessageTimestamp;
    }

    public void setDisableMessageTimestamp(boolean disableMessageTimestamp) {
        this.disableMessageTimestamp = disableMessageTimestamp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected ActiveMQMessageProducer getMessageProducer() {
        return messageProducer;
    }

    public String toString() {
        return "PooledProducer { "+messageProducer+" }";
    }

}
