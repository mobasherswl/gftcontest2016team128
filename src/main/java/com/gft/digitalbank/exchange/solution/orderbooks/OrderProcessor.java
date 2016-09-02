package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;

@FunctionalInterface
public interface OrderProcessor {
    void process(BrokerMessage brokerMessage);
}
