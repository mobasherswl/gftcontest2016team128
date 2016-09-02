package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;

import java.util.Set;

@FunctionalInterface
public interface OrderBookRetrievalService {
    Set<OrderBook> retrieveOrderBooks();
}
