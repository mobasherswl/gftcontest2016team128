package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.matcher.QueuesMatcher;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.gft.digitalbank.exchange.model.orders.Side.SELL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderBookProcessorImplTest {
    private OrderBookProcessor orderBookProcessor;
    @Mock
    private OrderCache orderCache;
    @Mock
    private QueuesMatcher orderEntryQueuesMatcher;
    @Mock
    private BuySellOrderQueue buySellOrderQueue;

    private static PositionOrder createPositionOrder(int id, int amount, int price, String broker, String product,
                                                     String client, Side side) {
        return PositionOrder.builder().id(id).broker(broker).timestamp(id).product(product).side(side).client(client)
                .details(OrderDetails.builder().price(price).amount(amount).build()).build();
    }

    private static ModificationOrder createModifcationOrder(int id, int amount, int price, String broker,
                                                            int modifiedOrderId) {
        return ModificationOrder.builder().id(id).broker(broker).timestamp(id).modifiedOrderId(modifiedOrderId)
                .details(OrderDetails.builder().price(price).amount(amount).build()).build();
    }

    private static CancellationOrder createCancellationOrder(int id, String broker,
                                                             int cancellationOrderId) {
        return CancellationOrder.builder().id(id).broker(broker).timestamp(id).cancelledOrderId(cancellationOrderId)
                .build();
    }

    private static OrderEntry createOrderEntry(PositionOrder positionOrder) {
        return OrderEntry.builder().id(positionOrder.getId()).amount(positionOrder.getDetails().getAmount())
                .broker(positionOrder.getBroker()).client(positionOrder.getClient())
                .price(positionOrder.getDetails().getPrice()).build();
    }

    @Before
    public void setUp() {
        orderBookProcessor = new OrderBookProcessorImpl("product", orderCache, orderEntryQueuesMatcher,
                buySellOrderQueue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void productCannotBeNull() {
        new OrderBookProcessorImpl(null, orderCache, orderEntryQueuesMatcher, buySellOrderQueue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void orderCacheCannotBeNull() {
        new OrderBookProcessorImpl("product", null, orderEntryQueuesMatcher, buySellOrderQueue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void orderEntryQueuesMatcherCannotBeNull() {
        new OrderBookProcessorImpl("product", orderCache, null, buySellOrderQueue);
    }

    @Test
    public void processPositionOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        orderBookProcessor.process(positionOrder);
        orderBookProcessor.retrieveOrderBook();
        verify(buySellOrderQueue).put(positionOrder);
    }

    @Test
    public void processModificationOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        ModificationOrder modificationOrder = createModifcationOrder(2, 2, 2, "broker1", 1);
        when(orderCache.get(positionOrder.getId())).thenReturn(positionOrder);
        when(buySellOrderQueue.remove(positionOrder)).thenReturn(true);
        orderBookProcessor.process(positionOrder);
        orderBookProcessor.process(modificationOrder);
        orderBookProcessor.retrieveOrderBook();
        verify(orderCache).get(positionOrder.getId());
        verify(buySellOrderQueue).remove(positionOrder);
        verify(buySellOrderQueue).put(positionOrder);
    }

    @Test
    public void processCancellationOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        CancellationOrder cancellationOrder = createCancellationOrder(2, "broker1", 1);
        when(orderCache.remove(positionOrder.getId())).thenReturn(positionOrder);
        when(buySellOrderQueue.remove(positionOrder)).thenReturn(true);
        orderBookProcessor.process(positionOrder);
        orderBookProcessor.process(cancellationOrder);
        orderBookProcessor.retrieveOrderBook();
        verify(orderCache).remove(positionOrder.getId());
        verify(buySellOrderQueue).remove(positionOrder);
    }

    @Test
    public void buySellOrdersAvailable() {
        PositionOrder sellPositionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder buyPositionOrder = createPositionOrder(2, 1, 1, "broker2", "product1", "client2", BUY);
        when(buySellOrderQueue.areBuySellOrdersAvailable()).thenReturn(true);
        orderBookProcessor.process(sellPositionOrder);
        orderBookProcessor.process(buyPositionOrder);
        orderBookProcessor.retrieveOrderBook();
        verify(buySellOrderQueue).put(sellPositionOrder);
        verify(buySellOrderQueue).put(buyPositionOrder);
    }

    @Test
    public void processOrderLogsException() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        when(buySellOrderQueue.areBuySellOrdersAvailable()).thenReturn(true);
        doThrow(RuntimeException.class).when(orderEntryQueuesMatcher).match(buySellOrderQueue);
        orderBookProcessor.process(positionOrder);
        orderBookProcessor.retrieveOrderBook();
    }

    @Test
    public void retrieveOrderBooksShouldReturnOrderBook() {
        PositionOrder sellPositionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder buyPositionOrder = createPositionOrder(2, 2, 2, "broker2", "product1", "client2", BUY);
        OrderEntry buyOrderEntry = createOrderEntry(buyPositionOrder);
        OrderEntry sellOrderEntry = createOrderEntry(sellPositionOrder);
        OrderBook orderBook = OrderBook.builder().product("product1").buyEntries(Collections.singletonList(buyOrderEntry))
                .sellEntries(Collections.singletonList(sellOrderEntry)).build();
        when(buySellOrderQueue.retrieveOrderBook()).thenReturn(orderBook);
        orderBookProcessor.process(sellPositionOrder);
        orderBookProcessor.process(buyPositionOrder);
        assertEquals(orderBook, orderBookProcessor.retrieveOrderBook());
    }
}