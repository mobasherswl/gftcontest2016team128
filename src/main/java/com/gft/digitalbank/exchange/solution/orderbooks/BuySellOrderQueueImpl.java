package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.google.common.base.Preconditions.checkArgument;

public class BuySellOrderQueueImpl implements BuySellOrderQueue {
    private static final int SIZE = 16;
    private final PriorityBlockingQueue<PositionOrder> buyOrderPriorityBlockingQueue;
    private final PriorityBlockingQueue<PositionOrder> sellOrderPriorityBlockingQueue;
    private final OrderCache orderCache;
    private final String product;

    public BuySellOrderQueueImpl(OrderCache orderCache, String product) {
        checkArgument(StringUtils.isNoneBlank(product), "Product cannot be null, empty or contains spaces only");
        checkArgument(orderCache != null, "OrderCache can not be null");

        this.orderCache = orderCache;
        this.product = product;
        buyOrderPriorityBlockingQueue = new PriorityBlockingQueue<>(SIZE, getBuyPositionOrderComparator());
        sellOrderPriorityBlockingQueue = new PriorityBlockingQueue<>(SIZE, getSellPositionOrderComparator());
    }

    @Override
    public void put(PositionOrder positionOrder) {
        orderCache.put(positionOrder);
        if (BUY == positionOrder.getSide()) {
            buyOrderPriorityBlockingQueue.put(positionOrder);
        } else {
            sellOrderPriorityBlockingQueue.put(positionOrder);
        }
    }

    @Override
    public boolean remove(PositionOrder positionOrder) {
        if (BUY == positionOrder.getSide()) {
            return buyOrderPriorityBlockingQueue.remove(positionOrder);
        } else {
            return sellOrderPriorityBlockingQueue.remove(positionOrder);
        }
    }

    @Override
    public boolean areBuySellOrdersAvailable() {
        return !(buyOrderPriorityBlockingQueue.isEmpty() || sellOrderPriorityBlockingQueue.isEmpty());
    }

    @Override
    public OrderBook retrieveOrderBook() {
        return new OrderBook(product, getBuyOrderEntries(), getSellOrderEntries());
    }

    @Override
    public Queue<PositionOrder> get(Side side) {
        if (BUY == side) {
            return buyOrderPriorityBlockingQueue;
        } else {
            return sellOrderPriorityBlockingQueue;
        }
    }

    private OrderEntry of(int orderEntryId, PositionOrder orderEntry) {
        return new OrderEntry(orderEntryId, orderEntry.getBroker(),
                orderEntry.getDetails().getAmount(), orderEntry.getDetails().getPrice(),
                orderEntry.getClient());
    }

    private List<OrderEntry> getBuyOrderEntries() {
        return toOrderEntries(buyOrderPriorityBlockingQueue);
    }

    private List<OrderEntry> getSellOrderEntries() {
        return toOrderEntries(sellOrderPriorityBlockingQueue);
    }

    private List<OrderEntry> toOrderEntries(PriorityBlockingQueue<PositionOrder> queue) {
        final List<OrderEntry> orderEntries = new ArrayList<>();
        Integer entryOrderIdGenerator = 1;
        for (PositionOrder order = queue.poll(); order != null; order = queue.poll()) {
            orderEntries.add(of(entryOrderIdGenerator++, order));
        }
        return orderEntries;
    }

    private Comparator<PositionOrder> getBuyPositionOrderComparator() {
        return (o1, o2) -> {
            int comparison = Integer.compare(o2.getDetails().getPrice(), o1.getDetails().getPrice());
            if (comparison == 0) {
                comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
            return comparison;
        };
    }

    private Comparator<PositionOrder> getSellPositionOrderComparator() {
        return (o1, o2) -> {
            int comparison = Integer.compare(o1.getDetails().getPrice(), o2.getDetails().getPrice());
            if (comparison == 0) {
                comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
            return comparison;
        };
    }
}
