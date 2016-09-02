package com.gft.digitalbank.exchange.solution.orderbooks.cache;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;

public interface OrderCache {
    void put(PositionOrder positionOrder);

    PositionOrder get(int positionOrderId);

    PositionOrder remove(int positionOrderId);

    boolean contains(int positionOrderId);
}
