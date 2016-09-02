package com.gft.digitalbank.exchange.solution.test;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.model.orders.Side;

import java.util.Random;

public class OrderMessageUtils {
    private static final String CLIENT = "client";
    private static final String PRODUCT = "product";
    private static final Integer MODIFIED_ORDER_ID = 3;

    public static final String BROKER_1 = "1";
    public static final String BROKER_2 = "2";
    public static final ShutdownNotification SHUTDOWN_1 = ShutdownNotification.builder().id(1).timestamp(1).broker("shutdown").build();

    public static ModificationOrder createModificationOrder(int id) {
        return ModificationOrder.builder().id(id).timestamp(id).modifiedOrderId(MODIFIED_ORDER_ID).broker(getRandomBroker())
                .details(OrderDetails.builder().amount(getRandomAmount()).price(getRandomPrice()).build()).build();
    }

    public static PositionOrder createPositionOrder(int id) {
        return createPositionOrder(id, getRandomBroker());
    }

    public static PositionOrder createPositionOrder(int id, String broker) {
        return PositionOrder.builder().id(id).timestamp(id).client(CLIENT).broker(broker).side(getRandomSide()).product(PRODUCT)
                .details(OrderDetails.builder().amount(getRandomAmount()).price(getRandomPrice()).build()).build();
    }

    public static ShutdownNotification createShutdownNotification(int id, String broker) {
        return ShutdownNotification.builder().id(id).timestamp(id).broker(broker).build();
    }


    private static String getRandomBroker() {
        return new Random(9).nextInt(2) == 0 ? BROKER_1 : BROKER_2;
    }

    private static Side getRandomSide() {
        return new Random(59).nextInt(2) == 0 ? Side.BUY : Side.SELL;
    }

    private static int getRandomAmount() {
        return new Random(30).nextInt(10) * 10;
    }

    private static int getRandomPrice() {
        return new Random(65).nextInt(8) * 10;
    }
}
