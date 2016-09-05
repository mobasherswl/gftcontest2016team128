package com.gft.digitalbank.exchange.solution.message;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static com.gft.digitalbank.exchange.model.orders.MessageType.SHUTDOWN_NOTIFICATION;

@Singleton
public class OrderedBrokerMessageStorage implements MessageStorage<BrokerMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderedBrokerMessageStorage.class);
    private static final String MESSAGE_AVAILABLE = "messageAvailable";
    private final AtomicInteger nextMessageId = new AtomicInteger(1);
    private final ConcurrentBlockingHashMap<Integer, BrokerMessage> brokerMessageMap;
    private final ConcurrentBlockingHashMap<String, Object> anyMessageArrivalMap;
    private AtomicInteger shutdownCounter = new AtomicInteger();
    private ShutdownNotification shutdownNotification = ShutdownNotification.builder().id(1).broker("shutdown")
            .timestamp(1).build();


    public OrderedBrokerMessageStorage() {
        brokerMessageMap = new ConcurrentBlockingHashMap<>();
        anyMessageArrivalMap = new ConcurrentBlockingHashMap<>(2);
    }

    @Override
    public void add(BrokerMessage message) {
        try {
            LOG.info("queued {} of type {} for broker {}", message.getId(), message.getMessageType(), message.getBroker());
            boolean isNotAvailable = !isAnyMessageAvailable();
            if (SHUTDOWN_NOTIFICATION == message.getMessageType()) {
                shutdownCounter.incrementAndGet();
            } else {
                brokerMessageMap.put(message.getId(), message);
            }
            if(isNotAvailable) {
                anyMessageArrivalMap.put(MESSAGE_AVAILABLE, MESSAGE_AVAILABLE);
            }
        } catch (Exception e) {
            LOG.error("Error occurred while queueing", e);
        }
    }

    @Override
    public BrokerMessage get() {
        waitForAnyMessage();
        if (shouldProcessShutDown()) {
            LOG.info("shutdown message dequeued...");
            shutdownCounter.decrementAndGet();
            return shutdownNotification;
        } else {
            try {
                BrokerMessage brokerMessage = brokerMessageMap.removeOrBlock(nextMessageId.get());
                nextMessageId.incrementAndGet();
                return brokerMessage;
            } catch (InterruptedException e) {
                LOG.error("", e);
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    private void waitForAnyMessage() {
        while (!isAnyMessageAvailable()) {
            try {
                anyMessageArrivalMap.removeOrBlock(MESSAGE_AVAILABLE);
            } catch (InterruptedException e) {
                LOG.error("", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isAnyMessageAvailable() {
        return shutdownCounter.get() != 0 || !brokerMessageMap.isEmpty();
    }

    private boolean shouldProcessShutDown() {
        return shutdownCounter.get() != 0 && brokerMessageMap.isEmpty();
    }

}
