package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;

public interface OrderBookProcessor {
    void process(BrokerMessage brokerMessage);

    OrderBook retrieveOrderBook();
}
