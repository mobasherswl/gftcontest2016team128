package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.gft.digitalbank.exchange.model.orders.MessageType.*;

@Singleton
public class OrderProcessorImpl implements OrderProcessor, OrderBookRetrievalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProcessorImpl.class);
    private final Map<String, OrderBookProcessor> orderBookProcessorMap;
    private final OrderCache orderCache;
    private final OrderBookProcessorFactory orderBookProcessorFactory;

    @Inject
    OrderProcessorImpl(OrderCache orderCache, OrderBookProcessorFactory orderBookProcessorFactory) {
        orderBookProcessorMap = new ConcurrentHashMap<>();
        this.orderBookProcessorFactory = orderBookProcessorFactory;
        this.orderCache = orderCache;
    }


    private void processCancelOrder(CancellationOrder cancellationOrder) {
        processOrder(cancellationOrder.getCancelledOrderId(), cancellationOrder);
    }

    private void processModifyOrder(ModificationOrder modificationOrder) {
        processOrder(modificationOrder.getModifiedOrderId(), modificationOrder);
    }

    private void processOrder(int orderId, BrokerMessage brokerMessage) {
        PositionOrder positionOrder = orderCache.get(orderId);
        if (positionOrder != null) {
            OrderBookProcessor orderBookProcessor =
                    orderBookProcessorMap.get(positionOrder.getProduct());
            if (orderBookProcessor != null) {
                orderBookProcessor.process(brokerMessage);
            }
        }
    }

    private void processOrder(PositionOrder positionOrder) {
        orderCache.put(positionOrder);
        OrderBookProcessor orderBookProcessor = orderBookProcessorMap.get(positionOrder.getProduct());
        if (orderBookProcessor == null) {
            orderBookProcessor = orderBookProcessorFactory.get(positionOrder.getProduct());
            orderBookProcessorMap.put(positionOrder.getProduct(), orderBookProcessor);
        }
        orderBookProcessor.process(positionOrder);
    }

    @Override
    public void process(BrokerMessage brokerMessage) {
        LOGGER.info("Message received:{}", brokerMessage);
        if (ORDER == brokerMessage.getMessageType()) {
            processOrder((PositionOrder) brokerMessage);
        } else if (MODIFICATION == brokerMessage.getMessageType()) {
            processModifyOrder((ModificationOrder) brokerMessage);
        } else if (CANCEL == brokerMessage.getMessageType()) {
            processCancelOrder((CancellationOrder) brokerMessage);
        }
    }

    @Override
    public Set<OrderBook> retrieveOrderBooks() {
        LOGGER.info("OrderBooks retrieval called");
        return orderBookProcessorMap.values().parallelStream().map(
                OrderBookProcessor::retrieveOrderBook).filter(
                orderBook -> !(orderBook.getBuyEntries().isEmpty() && orderBook.getSellEntries().isEmpty()))
                .collect(Collectors.toSet());
    }
}
