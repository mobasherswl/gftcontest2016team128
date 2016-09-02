package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.orders.CancellationOrder;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.gft.digitalbank.exchange.model.orders.Side.SELL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderProcessorImplTest {
    @InjectMocks
    OrderProcessorImpl orderProcessor;
    @Mock
    OrderCache orderCache;
    @Mock
    OrderBookProcessorFactoryImpl orderBookProcessorFactory;

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

    @Test
    public void processOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        OrderBookProcessor orderBookProcessor = mock(OrderBookProcessor.class);
        when(orderBookProcessorFactory.get(anyString())).thenReturn(orderBookProcessor);
        orderProcessor.process(positionOrder);
        verify(orderCache, times(1)).put(positionOrder);
        verify(orderBookProcessorFactory, times(1)).get(anyString());
        verify(orderBookProcessor, times(1)).process(positionOrder);
    }

    @Test
    public void processModificationOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        ModificationOrder modificationOrder = createModifcationOrder(2, 2, 2, "broker1", 1);
        OrderBookProcessor orderBookProcessor = mock(OrderBookProcessor.class);
        when(orderBookProcessorFactory.get(anyString())).thenReturn(orderBookProcessor);
        when(orderCache.get(1)).thenReturn(positionOrder);
        orderProcessor.process(positionOrder);
        orderProcessor.process(modificationOrder);
        verify(orderCache, times(1)).get(positionOrder.getId());
        verify(orderBookProcessor, times(1)).process(modificationOrder);
    }

    @Test
    public void processCancellationOrder() {
        PositionOrder positionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        CancellationOrder cancellationOrder = createCancellationOrder(2, "broker1", 1);
        OrderBookProcessor orderBookProcessor = mock(OrderBookProcessor.class);
        when(orderBookProcessorFactory.get(anyString())).thenReturn(orderBookProcessor);
        when(orderCache.get(1)).thenReturn(positionOrder);
        orderProcessor.process(positionOrder);
        orderProcessor.process(cancellationOrder);
        verify(orderCache, times(1)).get(positionOrder.getId());
        verify(orderBookProcessor, times(1)).process(cancellationOrder);
    }

    @Test
    public void retrieveOrderBooksShouldNotReturnNull() {
        assertNotNull(orderProcessor.retrieveOrderBooks());
    }

    @Test
    public void retrieveOrderBooksShouldReturnOrderBook() {
        PositionOrder sellPositionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder buyPositionOrder = createPositionOrder(2, 2, 2, "broker2", "product1", "client2", BUY);
        OrderBookProcessor orderBookProcessor = mock(OrderBookProcessor.class);
        OrderEntry buyOrderEntry = createOrderEntry(buyPositionOrder);
        OrderEntry sellOrderEntry = createOrderEntry(sellPositionOrder);
        OrderBook orderBook = OrderBook.builder().product("product1").buyEntries(Arrays.asList(buyOrderEntry))
                .sellEntries(Arrays.asList(sellOrderEntry)).build();
        when(orderBookProcessorFactory.get(anyString())).thenReturn(orderBookProcessor);
        when(orderBookProcessor.retrieveOrderBook()).thenReturn(orderBook);
        orderProcessor.process(sellPositionOrder);
        orderProcessor.process(buyPositionOrder);
        assertEquals(1, orderProcessor.retrieveOrderBooks().size());
    }

    @Test
    public void retrieveOrderBooksShouldNotReturnEmptyOrderBook() {
        PositionOrder sellPositionOrder = createPositionOrder(1, 1, 1, "broker1", "product1", "client1", SELL);
        PositionOrder buyPositionOrder = createPositionOrder(2, 2, 2, "broker2", "product1", "client2", BUY);
        OrderBookProcessor orderBookProcessor = mock(OrderBookProcessor.class);
        OrderBook orderBook = OrderBook.builder().product("product1").buyEntries(Arrays.asList())
                .sellEntries(Arrays.asList()).build();
        when(orderBookProcessorFactory.get(anyString())).thenReturn(orderBookProcessor);
        when(orderBookProcessor.retrieveOrderBook()).thenReturn(orderBook);
        orderProcessor.process(sellPositionOrder);
        orderProcessor.process(buyPositionOrder);
        assertEquals(0, orderProcessor.retrieveOrderBooks().size());
    }
}