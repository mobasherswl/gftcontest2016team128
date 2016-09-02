package com.gft.digitalbank.exchange.solution.orderbooks.cache;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.google.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class OrderCacheImpl implements OrderCache {
    private final Map<Integer, PositionOrder> positionOrderMap;

    public OrderCacheImpl() {
        this.positionOrderMap = new ConcurrentHashMap<>();
    }

    @Override
    public void put(PositionOrder positionOrder) {
        positionOrderMap.put(positionOrder.getId(), positionOrder);
    }

    @Override
    public PositionOrder get(int positionOrderId) {
        return positionOrderMap.get(positionOrderId);
    }

    @Override
    public PositionOrder remove(int positionOrderId) {
        return positionOrderMap.remove(positionOrderId);
    }

    @Override
    public boolean contains(int positionOrderId) {
        return positionOrderMap.containsKey(positionOrderId);
    }
}
