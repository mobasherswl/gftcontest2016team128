package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.solution.matcher.QueuesMatcher;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class OrderBookProcessorFactoryImpl implements Provider<OrderBookProcessor>, OrderBookProcessorFactory {
    private final OrderCache orderCache;
    private final QueuesMatcher positionOrderQueuesMatcher;

    @Inject
    public OrderBookProcessorFactoryImpl(OrderCache orderCache, QueuesMatcher positionOrderQueuesMatcher) {
        this.orderCache = orderCache;
        this.positionOrderQueuesMatcher = positionOrderQueuesMatcher;
    }

    @Override
    public OrderBookProcessor get() {
        return null;
    }

    @Override
    public OrderBookProcessor get(String product) {
        return new OrderBookProcessorImpl(product, orderCache, positionOrderQueuesMatcher,
                new BuySellOrderQueueImpl(orderCache, product));
    }
}
