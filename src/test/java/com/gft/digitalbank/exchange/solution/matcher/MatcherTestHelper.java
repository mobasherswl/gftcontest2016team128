package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.gft.digitalbank.exchange.model.orders.Side.SELL;

class MatcherTestHelper {
    static final String BUY_BROKER = "buy broker";
    static final String BUY_CLIENT = "buy client";
    static final String SELL_CLIENT = "sell client";
    static final String SELL_BROKER = "sell broker";
    static final String PRODUCT_NAME = "product";
    private static AtomicInteger ORDER_ID = new AtomicInteger(1);

    private static Comparator<PositionOrder> buyOrderComparator = (o1, o2) -> {
        int comparison = -Integer.compare(o1.getDetails().getPrice(), o2.getDetails().getPrice());
        if (comparison == 0) {
            comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
        return comparison;
    };
    private static Comparator<PositionOrder> sellOrderComparator = (o1, o2) -> {
        int comparison = Integer.compare(o1.getDetails().getPrice(), o2.getDetails().getPrice());
        if (comparison == 0) {
            comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
        return comparison;
    };

    static Queue<PositionOrder> createOrdersQueue(boolean empty) {
        return empty ? new ArrayDeque<>() : new ArrayDeque<>(1);
    }

    static Queue<PositionOrder> createBuyQueue(PositionOrder... orders) {
        return createQueue(buyOrderComparator, orders);
    }

    static Queue<PositionOrder> createSellQueue(PositionOrder... orders) {
        return createQueue(sellOrderComparator, orders);
    }

    static Queue<PositionOrder> createQueue(Comparator<PositionOrder> positionOrderComparator, PositionOrder... orders) {
        Queue<PositionOrder> queue = new PriorityQueue<>(orders.length, positionOrderComparator);
        queue.addAll(Arrays.asList(orders));
        return queue;
    }

    static Object[] getBuySellOrderAndResult(PositionOrder buyOrder, PositionOrder sellOrder, int amount, int price, boolean isBuyMatched, boolean isSellMatched) {
        return new Object[]{buyOrder, sellOrder, createBuySellResult(buyOrder, sellOrder, amount, price, isBuyMatched, isSellMatched)};
    }

    static BuySellResult createBuySellResult(PositionOrder buyOrder, PositionOrder sellOrder, int amount, int price, boolean isBuyMatched, boolean isSellMatched) {
        return new BuySellResult.BuySellResultBuilder().amount(amount).price(price).productName(PRODUCT_NAME)
                .buyOrder(buyOrder).sellOrder(sellOrder).buyFullyMatched(isBuyMatched).sellFullyMatched(isSellMatched).build();
    }

    static PositionOrder createBuyOrder(int amount, int price) {
        return createBuyOrder(ORDER_ID.getAndIncrement(), amount, price);
    }

    static PositionOrder createBuyOrder(int id, int amount, int price) {
        return createOrder(id, amount, price, BUY_BROKER, BUY_CLIENT, BUY);
    }

    static PositionOrder createSellOrder(int amount, int price) {
        return createSellOrder(ORDER_ID.getAndIncrement(), amount, price);
    }

    static PositionOrder createSellOrder(int id, int amount, int price) {
        return createOrder(id, amount, price, SELL_BROKER, SELL_CLIENT, SELL);
    }

    private static PositionOrder createOrder(int id, int amount, int price, String broker, String client, Side side) {
        return PositionOrder.builder().id(id).broker(broker).timestamp(id).product(PRODUCT_NAME).side(side).client(client).details(OrderDetails.builder().price(price).amount(amount).build()).build();
    }

}
