package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.matcher.QueuesMatcher;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.gft.digitalbank.exchange.model.orders.Side.SELL;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class BuySellOrderQueueImplTest {
    private BuySellOrderQueue buySellOrderQueue;
    @Mock
    private QueuesMatcher orderEntryQueuesMatcher;
    @Mock
    private OrderCache orderCache;

    private static PositionOrder createPositionOrder(int id, int amount, int price, String broker, String product,
                                                     String client, Side side) {
        return createPositionOrder(id, id, amount, price, broker, product, client, side);
    }

    private static PositionOrder createPositionOrder(int id, int timestamp, int amount, int price, String broker, String product,
                                                     String client, Side side) {
        return PositionOrder.builder().id(id).broker(broker).timestamp(timestamp).product(product).side(side).client(client)
                .details(OrderDetails.builder().price(price).amount(amount).build()).build();
    }

    private static OrderEntry createOrderEntry(int orderEntryId, PositionOrder positionOrder) {
        return OrderEntry.builder().id(orderEntryId).amount(positionOrder.getDetails().getAmount())
                .broker(positionOrder.getBroker()).client(positionOrder.getClient())
                .price(positionOrder.getDetails().getPrice()).build();
    }

    @Before
    public void setUp() {
        buySellOrderQueue = new BuySellOrderQueueImpl(orderCache, "product1");
    }

    @Test
    public void putPositionOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        buySellOrderQueue.put(positionOrder);
    }

    @Test
    public void removeSellPositionOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        buySellOrderQueue.put(positionOrder);
        assertTrue(buySellOrderQueue.remove(positionOrder));
    }

    @Test
    public void removeBuyPositionOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", BUY);
        buySellOrderQueue.put(positionOrder);
        assertTrue(buySellOrderQueue.remove(positionOrder));
    }

    @Test
    public void buySellOrdersAvailable() {
        PositionOrder buyPositionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder sellPositionOrder = createPositionOrder(2, 1, 1, "broker2", "product1", "client2", BUY);
        buySellOrderQueue.put(buyPositionOrder);
        buySellOrderQueue.put(sellPositionOrder);
        assertTrue(buySellOrderQueue.areBuySellOrdersAvailable());
    }


    @Test
    public void retrieveOrderBook() {
        PositionOrder buyPositionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder sellPositionOrder = createPositionOrder(2, 1, 1, "broker2", "product1", "client2", BUY);
        buySellOrderQueue.put(buyPositionOrder);
        buySellOrderQueue.put(sellPositionOrder);
        assertNotNull(buySellOrderQueue.retrieveOrderBook());
    }

    @Test
    public void buyOrderSequence() {
        PositionOrder positionOrder1 = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", BUY);
        PositionOrder positionOrder2 = createPositionOrder(2, 1, 1, "broker1", "product1", "client1", BUY);
        buySellOrderQueue.put(positionOrder1);
        buySellOrderQueue.put(positionOrder2);
        OrderBook orderBook = buySellOrderQueue.retrieveOrderBook();
        assertEquals(createOrderEntry(1, positionOrder2), orderBook.getBuyEntries().get(0));
    }

    @Test
    public void sellOrderSequence() {
        PositionOrder positionOrder1 = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder positionOrder2 = createPositionOrder(2, 1, 1, "broker1", "product1", "client1", SELL);
        buySellOrderQueue.put(positionOrder1);
        buySellOrderQueue.put(positionOrder2);
        OrderBook orderBook = buySellOrderQueue.retrieveOrderBook();
        assertEquals(createOrderEntry(1, positionOrder2), orderBook.getSellEntries().get(0));
    }

    @Test
    public void getBuyQueue() {
        assertNotNull(buySellOrderQueue.get(BUY));
    }

    @Test
    public void getSellQueue() {
        assertNotNull(buySellOrderQueue.get(SELL));
    }
}