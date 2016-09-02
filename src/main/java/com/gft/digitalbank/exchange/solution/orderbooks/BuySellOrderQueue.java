package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;

import java.util.Queue;

public interface BuySellOrderQueue {
    void put(PositionOrder positionOrder);

    boolean remove(PositionOrder positionOrder);

    boolean areBuySellOrdersAvailable();

    OrderBook retrieveOrderBook();

    Queue<PositionOrder> get(Side buy);
}
