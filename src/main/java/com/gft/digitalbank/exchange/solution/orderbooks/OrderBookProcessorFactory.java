package com.gft.digitalbank.exchange.solution.orderbooks;

@FunctionalInterface
public interface OrderBookProcessorFactory {
    OrderBookProcessor get(String product);
}
