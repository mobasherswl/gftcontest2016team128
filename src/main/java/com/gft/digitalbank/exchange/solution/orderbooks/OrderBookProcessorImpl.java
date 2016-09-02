package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.orders.*;
import com.gft.digitalbank.exchange.solution.matcher.QueuesMatcher;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.compare;

class OrderBookProcessorImpl implements OrderBookProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderBookProcessorImpl.class);
    private static final int SIZE = 16;
    private final PriorityBlockingQueue<BrokerMessage> orderPriorityBlockingQueue;
    private final BuySellOrderQueue buySellOrderQueue;
    private final OrderCache orderCache;
    private final QueuesMatcher orderEntryQueuesMatcher;
    private final SynchronousQueue<Object> synchronousQueue;
    private final String product;
    private volatile boolean isShutdown = false;

    OrderBookProcessorImpl(String product, OrderCache orderCache, QueuesMatcher orderEntryQueuesMatcher,
                           BuySellOrderQueue buySellOrderQueue) {
        checkArgument(!StringUtils.isBlank(product), "Product cannot be null, empty or contains spaces only");
        checkArgument(orderCache != null, "OrderCache cannot be null");
        checkArgument(orderEntryQueuesMatcher != null, "QueuesMatcher cannot be null");
        this.synchronousQueue = new SynchronousQueue<>();
        this.orderCache = orderCache;
        this.orderEntryQueuesMatcher = orderEntryQueuesMatcher;
        this.product = product;
        orderPriorityBlockingQueue = new PriorityBlockingQueue<>(SIZE, (o1, o2) -> compare(o1.getId(), o2.getId()));
        this.buySellOrderQueue = buySellOrderQueue;
        startUp();
    }

    @Override
    public void process(BrokerMessage brokerMessage) {
        LOGGER.info("Order Book {} received message {}", product, brokerMessage);
        if (!isShutdown) {
            orderPriorityBlockingQueue.put(brokerMessage);
        }
    }

    @Override
    public OrderBook retrieveOrderBook() {
        LOGGER.info("OrderBook {} retrieval called", product);
        process(ShutdownNotification.builder().
                id(Integer.MAX_VALUE).broker("shutdown").timestamp(System.currentTimeMillis() + 86400)
                .build());
        waitForCompletion();
        LOGGER.info("Retrieving OrderBook {}", product);
        return buySellOrderQueue.retrieveOrderBook();
    }

    private void waitForCompletion() {
        while (true) {
            try {
                synchronousQueue.take();
                break;
            } catch (InterruptedException e) {
                LOGGER.error("Waiting thread {} was interrupted while waiting for order book processing completion", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startUp() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                while (!isShutdown || !orderPriorityBlockingQueue.isEmpty()) {
                    try {
                        processOrder(orderPriorityBlockingQueue.take());
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
            } finally {
                notifyCompletion();
            }
        });
    }

    private void notifyCompletion() {
        Object object = new Object();
        while (true) {
            try {
                synchronousQueue.put(object);
                break;
            } catch (InterruptedException e) {
                LOGGER.error("Notifying thread {} was interrupted while signalling for order book processing completion", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processOrder(BrokerMessage brokerMessage) {
        processOrderByType(brokerMessage);
        if (buySellOrderQueue.areBuySellOrdersAvailable()) {
            orderEntryQueuesMatcher.match(buySellOrderQueue);
        }
    }

    private void processOrderByType(BrokerMessage brokerMessage) {
        switch (brokerMessage.getMessageType()) {
            case ORDER:
                putOrder((PositionOrder) brokerMessage);
                break;
            case MODIFICATION:
                modifyOrder((ModificationOrder) brokerMessage);
                break;
            case CANCEL:
                cancelOrder((CancellationOrder) brokerMessage);
                break;
            case SHUTDOWN_NOTIFICATION:
                isShutdown = true;
                break;
            default:
                break;
        }
    }

    private void modifyOrder(ModificationOrder modificationOrder) {
        PositionOrder positionOrder = orderCache.get(modificationOrder.getModifiedOrderId());
        if (positionOrder != null
                && positionOrder.getBroker().equals(modificationOrder.getBroker())
                && buySellOrderQueue.remove(positionOrder)) {
            positionOrder = PositionOrder.builder().id(positionOrder.getId()).product(positionOrder.getProduct())
                    .details(modificationOrder.getDetails()).timestamp(modificationOrder.getTimestamp())
                    .client(positionOrder.getClient()).broker(positionOrder.getBroker())
                    .side(positionOrder.getSide()).build();
            process(positionOrder);
        }
    }

    private void cancelOrder(CancellationOrder cancellationOrder) {
        PositionOrder positionOrder = orderCache.remove(cancellationOrder.getCancelledOrderId());
        if (positionOrder != null && positionOrder.getBroker().equals(cancellationOrder.getBroker())) {
            buySellOrderQueue.remove(positionOrder);
        }
    }

    private void putOrder(PositionOrder positionOrder) {
        buySellOrderQueue.put(positionOrder);
    }

}
