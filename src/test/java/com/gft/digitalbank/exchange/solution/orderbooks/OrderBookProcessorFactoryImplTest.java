package com.gft.digitalbank.exchange.solution.orderbooks;

import com.gft.digitalbank.exchange.solution.matcher.QueuesMatcher;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class OrderBookProcessorFactoryImplTest {
    @InjectMocks
    OrderBookProcessorFactoryImpl orderBookProcessorFactory;
    @Mock
    OrderCache orderCache;
    @Mock
    QueuesMatcher positionOrderQueuesMatcher;

    @Test
    public void getShouldReturnNull() {
        assertNull(orderBookProcessorFactory.get());
    }

    @Test
    public void getWithProductReturnsOrderBookProcessor() {
        assertNotNull(orderBookProcessorFactory.get("product"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWithNullProductFails() {
        assertNotNull(orderBookProcessorFactory.get(null));
    }
}