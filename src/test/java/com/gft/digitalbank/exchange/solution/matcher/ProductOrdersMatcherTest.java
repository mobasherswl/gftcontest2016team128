package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.orderbooks.BuySellOrderQueue;
import com.gft.digitalbank.exchange.solution.transactions.TransactionPersistenceService;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Comparator;
import java.util.Queue;

import static com.gft.digitalbank.exchange.solution.matcher.MatcherTestHelper.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class ProductOrdersMatcherTest {
    private TransactionPersistenceService transactionServiceMock = mock(TransactionPersistenceService.class);
    private BuySellPairMatcher pairMatcherMock = mock(BuySellPairMatcher.class);

    private ProductOrdersMatcher productOrdersMatcher = new ProductOrdersMatcher(pairMatcherMock, transactionServiceMock);
    private Comparator<PositionOrder> buyOrderComparator = (o1, o2) -> {
        int comparison = -Integer.compare(o1.getDetails().getPrice(), o2.getDetails().getPrice());
        if (comparison == 0) {
            comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
        return comparison;
    };
    private Comparator<PositionOrder> sellOrderComparator = (o1, o2) -> {
        int comparison = Integer.compare(o1.getDetails().getPrice(), o2.getDetails().getPrice());
        if (comparison == 0) {
            comparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
        return comparison;
    };

    public Object[] ordersArraySizes() {
        return new Object[][]{
                {true, false}, {false, true}, {true, true}
        };
    }

    @Before
    public void setUp() throws Exception {
        reset(pairMatcherMock, transactionServiceMock);
    }

    @Test
    @Parameters(method = "ordersArraySizes")
    public void shouldNotStartMatchingWhenAtLeastOneOfBuySellOrdersIsEmpty(boolean buysIsEmpty, boolean sellsIsEmpty) throws Exception {
        Queue<PositionOrder> buyEntries = createOrdersQueue(buysIsEmpty);
        Queue<PositionOrder> sellEntries = createOrdersQueue(sellsIsEmpty);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verifyNoMoreInteractions(pairMatcherMock);
        verifyNoMoreInteractions(transactionServiceMock);
    }

    @Test
    public void shouldCheckFirstPolledBuySellOrdersMatchedAndCreateNewTransaction() throws Exception {
        PositionOrder buyOrder = createBuyOrder(1, 1);
        PositionOrder sellOrder = createSellOrder(1, 1);
        BuySellResult resultMock = createBuySellResult(buyOrder, sellOrder, 1, 1, true, true);
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(resultMock);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, times(1)).save(resultMock);
        assertTrue(buyEntries.isEmpty());
        assertTrue(sellEntries.isEmpty());
    }

    @Test
    public void shouldCheckFirstPolledBuySellOrdersNotMatchAndNoTransactionSaved() throws Exception {
        PositionOrder buyOrder = createBuyOrder(1, 5);
        PositionOrder sellOrder = createSellOrder(1, 10);
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(BuySellResult.EMPTY);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, never()).save(any());
        assertTrue(buyEntries.contains(buyOrder));
        assertTrue(sellEntries.contains(sellOrder));
    }

    @Test
    public void shouldCheckBuyOrderUpdatedWhenFirstPolledBuySellOrdersNotFullMatchBuy() throws Exception {
        PositionOrder buyOrder = createBuyOrder(5, 5);
        PositionOrder sellOrder = createSellOrder(1, 10);
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder);
        BuySellResult result = createBuySellResult(buyOrder, sellOrder, 1, 1, false, true);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(result);
        PositionOrder updatedBuyOrder = createBuyOrder(buyOrder.getId(), 5 - 1, 5);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, times(1)).save(result);
        assertTrue(buyEntries.contains(updatedBuyOrder));
        assertTrue(sellEntries.isEmpty());
    }

    @Test
    public void shouldCheckSellOrderUpdatedWhenFirstPolledBuySellOrdersNotFullMatchSell() throws Exception {
        PositionOrder buyOrder = createBuyOrder(1, 5);
        PositionOrder sellOrder = createSellOrder(7, 10);
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder);
        BuySellResult result = createBuySellResult(buyOrder, sellOrder, 1, 1, true, false);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(result);
        PositionOrder updatedSellOrder = createSellOrder(sellOrder.getId(), 7 - 1, 10);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, times(1)).save(result);
        verify(transactionServiceMock, times(1)).save(any());
        assertTrue(buyEntries.isEmpty());
        assertTrue(sellEntries.contains(updatedSellOrder));
    }

    @Test
    public void shouldGetAnotherBuyAndRepeatIterationAfterFirstPolledBuySellOrdersNotFullMatchedSell() throws Exception {
        PositionOrder buyOrder = createBuyOrder(1, 10);
        PositionOrder sellOrder = createSellOrder(7, 5);
        PositionOrder buyOrder2 = createBuyOrder(6, 10);
        PositionOrder updatedSellOrder = createSellOrder(sellOrder.getId(), sellOrder.getDetails().getAmount() - buyOrder.getDetails().getAmount(), sellOrder.getDetails().getPrice());
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder, buyOrder2);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder);
        BuySellResult result = createBuySellResult(buyOrder, sellOrder, 1, 10, true, false);
        BuySellResult result2 = createBuySellResult(buyOrder2, updatedSellOrder, 6, 5, true, true);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(result);
        when(pairMatcherMock.getMatchResult(buyOrder2, updatedSellOrder)).thenReturn(result2);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, times(1)).save(result);
        verify(transactionServiceMock, times(1)).save(result2);
        assertTrue(buyEntries.isEmpty());
        assertTrue(sellEntries.isEmpty());
    }

    @Test
    public void shouldGetAnotherSellAndRepeatIterationAfterFirstPolledBuySellOrdersNotFullMatchedBuy() throws Exception {
        PositionOrder buyOrder = createBuyOrder(5, 10);
        PositionOrder sellOrder = createSellOrder(2, 5);
        PositionOrder updatedBuyOrder = createBuyOrder(buyOrder.getId(), buyOrder.getDetails().getAmount() - sellOrder.getDetails().getAmount(), buyOrder.getDetails().getPrice());
        PositionOrder sellOrder2 = createSellOrder(3, 5);
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder, sellOrder2);
        BuySellResult result = createBuySellResult(buyOrder, sellOrder, 2, 10, false, true);
        BuySellResult result2 = createBuySellResult(updatedBuyOrder, sellOrder2, 3, 10, true, true);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(result);
        when(pairMatcherMock.getMatchResult(updatedBuyOrder, sellOrder2)).thenReturn(result2);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, times(1)).save(result);
        verify(transactionServiceMock, times(1)).save(result2);
        assertTrue(buyEntries.isEmpty());
        assertTrue(sellEntries.isEmpty());
    }

    @Test
    public void shouldDoNothingWhenNoMatches() throws Exception {
        PositionOrder buyOrder = createBuyOrder(5, 10);
        PositionOrder sellOrder = createSellOrder(5, 20);
        Queue<PositionOrder> buyEntries = createQueue(buyOrderComparator, buyOrder);
        Queue<PositionOrder> sellEntries = createQueue(sellOrderComparator, sellOrder);
        when(pairMatcherMock.getMatchResult(buyOrder, sellOrder)).thenReturn(BuySellResult.EMPTY);
        BuySellOrderQueue orderQueue = OrderQueueFactory.create(buyEntries, sellEntries);

        productOrdersMatcher.match(orderQueue);

        verify(transactionServiceMock, times(0)).save(any());
        assertTrue(buyEntries.contains(buyOrder));
        assertTrue(sellEntries.contains(sellOrder));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenPairMatcherIsNull() throws Exception {
        new ProductOrdersMatcher(null, transactionServiceMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTransactionServiceIsNull() throws Exception {
        new ProductOrdersMatcher(pairMatcherMock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenOrdersIsNull() throws Exception {
        productOrdersMatcher.match(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenBuyEntriesIsNull() throws Exception {
        productOrdersMatcher.match(OrderQueueFactory.create(null, createOrdersQueue(true)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSellEntriesIsNull() throws Exception {
        productOrdersMatcher.match(OrderQueueFactory.create(createOrdersQueue(true), null));
    }

}

class OrderQueueFactory {
    static BuySellOrderQueue create(Queue<PositionOrder> buyQueue, Queue<PositionOrder> sellQueue) {
        return new BuySellOrderQueue() {
            @Override
            public void put(PositionOrder positionOrder) {
                switch (positionOrder.getSide()) {
                    case BUY:
                        buyQueue.add(positionOrder);
                        break;
                    case SELL:
                        sellQueue.add(positionOrder);
                        break;
                }
            }

            @Override
            public boolean remove(PositionOrder positionOrder) {
                switch (positionOrder.getSide()) {
                    case BUY:
                        buyQueue.remove(positionOrder);
                        break;
                    case SELL:
                        sellQueue.remove(positionOrder);
                        break;
                }
                return false;
            }

            @Override
            public Queue<PositionOrder> get(Side buy) {
                switch (buy) {
                    case BUY:
                        return buyQueue;
                    case SELL:
                        return sellQueue;
                }
                return null;
            }

            @Override
            public boolean areBuySellOrdersAvailable() {
                return false;
            }

            @Override
            public OrderBook retrieveOrderBook() {
                return null;
            }

        };
    }
}
