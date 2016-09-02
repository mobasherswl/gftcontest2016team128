package com.gft.digitalbank.exchange.solution.orderbooks.cache;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.gft.digitalbank.exchange.model.orders.Side.SELL;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class OrderCacheImplTest {
    private OrderCache orderCache;
    private PositionOrder positionOrder;

    @Before
    public void setUp() {
        positionOrder = PositionOrder.builder().id(1).broker("broker").timestamp(1).product("product").side(SELL)
                .client("client").details(OrderDetails.builder().price(1).amount(1).build()).build();
        orderCache = new OrderCacheImpl();
    }

    @Test
    public void put() {
        orderCache.put(positionOrder);
        assertEquals(positionOrder, orderCache.get(positionOrder.getId()));
    }

    @Test
    public void remove() {
        orderCache.put(positionOrder);
        orderCache.remove(positionOrder.getId());
        assertNull(null, orderCache.get(positionOrder.getId()));
    }

    @Test
    public void contains() {
        orderCache.put(positionOrder);
        assertTrue(orderCache.contains(positionOrder.getId()));
    }
}