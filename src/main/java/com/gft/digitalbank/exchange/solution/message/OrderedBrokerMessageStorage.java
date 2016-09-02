package com.gft.digitalbank.exchange.solution.message;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.gft.digitalbank.exchange.model.orders.MessageType.SHUTDOWN_NOTIFICATION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Singleton
public class OrderedBrokerMessageStorage implements MessageStorage<BrokerMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderedBrokerMessageStorage.class);
    private final AtomicInteger nextMessageId = new AtomicInteger(1);
    private final Map<Integer, BrokerMessage> brokerMessageMap;
    private final Lock lock;
    private Condition nextMessageCondition;
    private AtomicInteger shutdownCounter = new AtomicInteger();
    private ShutdownNotification shutdownNotification = ShutdownNotification.builder().id(1).broker("shutdown")
            .timestamp(1).build();

    public OrderedBrokerMessageStorage() {
        brokerMessageMap = new ConcurrentHashMap<>();
        lock = new ReentrantLock();
        nextMessageCondition = lock.newCondition();
    }

    @Override
    public void add(BrokerMessage message) {
        try {
            LOG.info("queued {} of type {} for broker {}", message.getId(), message.getMessageType(), message.getBroker());
            if (SHUTDOWN_NOTIFICATION == message.getMessageType()) {
                shutdownCounter.incrementAndGet();
            } else {
                brokerMessageMap.put(message.getId(), message);
                signalOnMessageArrival(message);
            }
        } catch (Exception e) {
            LOG.error("Error occurred while queueing", e);
        }
    }

    private void signalOnMessageArrival(BrokerMessage message) {
        if (nextMessageId.intValue() == message.getId()) {
            lock.lock();
            try {
                nextMessageCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public BrokerMessage get() {
        if (canDequeueFromShutDown()) {
            LOG.info("shutdown message dequeued...");
            shutdownCounter.decrementAndGet();
            return shutdownNotification;
        } else {
            return getMessage();
        }
    }

    private BrokerMessage getMessage() {
        BrokerMessage brokerMessage;
        int nextId = nextMessageId.intValue();
        while ((brokerMessage = brokerMessageMap.get(nextId)) == null && !canDequeueFromShutDown()) {
            lock.lock();
            try {
                nextMessageCondition.await(1, MILLISECONDS);
            } catch (InterruptedException e) {
                LOG.error("", e);
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
        if (brokerMessage != null) {
            brokerMessageMap.remove(nextId);
            nextMessageId.incrementAndGet();
        }
        return brokerMessage;
    }


    private boolean canDequeueFromShutDown() {
        return shutdownCounter.get() != 0 && brokerMessageMap.isEmpty();
    }

}
