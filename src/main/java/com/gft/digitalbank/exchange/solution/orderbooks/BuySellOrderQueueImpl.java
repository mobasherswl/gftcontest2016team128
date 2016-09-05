package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiFunction;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.google.common.base.Preconditions.checkArgument;

public class BuySellOrderQueueImpl implements BuySellOrderQueue {
    private static final int SIZE = 16;
    private final PriorityQueue<PositionOrder> buyOrderPriorityQueue;
    private final PriorityQueue<PositionOrder> sellOrderPriorityQueue;
    private final OrderCache orderCache;
    private final String product;

    public BuySellOrderQueueImpl(OrderCache orderCache, String product) {
        checkArgument(StringUtils.isNoneBlank(product), "Product cannot be null, empty or contains spaces only");
        checkArgument(orderCache != null, "OrderCache can not be null");

        this.orderCache = orderCache;
        this.product = product;
        buyOrderPriorityQueue = new PriorityQueue<>(SIZE, getPositionOrderComparator((o1, o2) -> Integer.compare(o2.getDetails().getPrice(), o1.getDetails().getPrice())));
        sellOrderPriorityQueue = new PriorityQueue<>(SIZE, getPositionOrderComparator((o1, o2) -> Integer.compare(o1.getDetails().getPrice(), o2.getDetails().getPrice())));
    }

    @Override
    public void put(PositionOrder positionOrder) {
        orderCache.put(positionOrder);
        if (BUY == positionOrder.getSide()) {
            buyOrderPriorityQueue.add(positionOrder);
        } else {
            sellOrderPriorityQueue.add(positionOrder);
        }
    }

    @Override
    public boolean remove(PositionOrder positionOrder) {
        if (BUY == positionOrder.getSide()) {
            return buyOrderPriorityQueue.remove(positionOrder);
        } else {
            return sellOrderPriorityQueue.remove(positionOrder);
        }
    }

    @Override
    public boolean areBuySellOrdersAvailable() {
        return !(buyOrderPriorityQueue.isEmpty() || sellOrderPriorityQueue.isEmpty());
    }

    @Override
    public OrderBook retrieveOrderBook() {
        return new OrderBook(product, getBuyOrderEntries(), getSellOrderEntries());
    }

    @Override
    public Queue<PositionOrder> get(Side side) {
        if (BUY == side) {
            return buyOrderPriorityQueue;
        } else {
            return sellOrderPriorityQueue;
        }
    }

    private OrderEntry of(int orderEntryId, PositionOrder orderEntry) {
        return new OrderEntry(orderEntryId, orderEntry.getBroker(),
                orderEntry.getDetails().getAmount(), orderEntry.getDetails().getPrice(),
                orderEntry.getClient());
    }

    private List<OrderEntry> getBuyOrderEntries() {
        return toOrderEntries(buyOrderPriorityQueue);
    }

    private List<OrderEntry> getSellOrderEntries() {
        return toOrderEntries(sellOrderPriorityQueue);
    }

    private List<OrderEntry> toOrderEntries(PriorityQueue<PositionOrder> queue) {
        final List<OrderEntry> orderEntries = new ArrayList<>();
        int entryOrderIdGenerator = 1;
        for (PositionOrder order = queue.poll(); order != null; order = queue.poll()) {
            orderEntries.add(of(entryOrderIdGenerator++, order));
        }
        return orderEntries;
    }

    private Comparator<PositionOrder> getPositionOrderComparator(BiFunction<PositionOrder, PositionOrder, Integer> compareBiFunction) {
        return (o1, o2) -> {
            int comparison = compareBiFunction.apply(o1, o2);
            if (comparison == 0) {
                comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
            return comparison;
        };
    }

}
