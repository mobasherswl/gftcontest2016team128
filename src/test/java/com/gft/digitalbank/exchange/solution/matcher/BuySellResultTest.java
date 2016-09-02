package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.gft.digitalbank.exchange.model.orders.Side.SELL;
import static com.gft.digitalbank.exchange.solution.matcher.MatcherTestHelper.*;

@RunWith(JUnit4.class)
public class BuySellResultTest {

    @Test
    public void shouldTestBusSellResultIsCreatedWhenBuyFullyMatched() throws Exception {
        PositionOrder buyOrder = PositionOrder.builder().id(1).broker(BUY_BROKER).timestamp(1).product(PRODUCT_NAME).side(BUY).client(BUY_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();
        PositionOrder sellOrder = PositionOrder.builder().id(1).broker(SELL_BROKER).timestamp(2).product(PRODUCT_NAME).side(SELL).client(SELL_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();

        BuySellResult.builder().amount(1).price(1).productName(PRODUCT_NAME).buyOrder(buyOrder).sellOrder(sellOrder).buyFullyMatched(true).build();
    }

    @Test
    public void shouldTestBusSellResultIsCreatedWhenSellFullyMatched() throws Exception {
        PositionOrder buyOrder = PositionOrder.builder().id(1).broker(BUY_BROKER).timestamp(1).product(PRODUCT_NAME).side(BUY).client(BUY_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();
        PositionOrder sellOrder = PositionOrder.builder().id(1).broker(SELL_BROKER).timestamp(2).product(PRODUCT_NAME).side(SELL).client(SELL_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();

        BuySellResult.builder().amount(1).price(1).productName(PRODUCT_NAME).buyOrder(buyOrder).sellOrder(sellOrder).sellFullyMatched(true).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAmountIsNotGreaterThenZero() throws Exception {
        BuySellResult.builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenPriceIsNotGreaterThenZero() throws Exception {
        BuySellResult.builder().amount(1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenProductNameIsNull() throws Exception {
        BuySellResult.builder().amount(1).price(1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenProductNameIsEmpty() throws Exception {
        BuySellResult.builder().amount(1).price(1).productName("").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenBuyOrderIsNull() throws Exception {
        BuySellResult.builder().amount(1).price(1).productName(PRODUCT_NAME).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSellOrderIsNull() throws Exception {
        PositionOrder buyOrder = PositionOrder.builder().id(1).broker(BUY_BROKER).timestamp(1).product(PRODUCT_NAME).side(BUY).client(BUY_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();

        BuySellResult.builder().amount(1).price(1).productName(PRODUCT_NAME).buyOrder(buyOrder).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenNoOneOfSellBuyOrdersIsFullyMatched() throws Exception {
        PositionOrder buyOrder = PositionOrder.builder().id(1).broker(BUY_BROKER).timestamp(1).product(PRODUCT_NAME).side(BUY).client(BUY_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();
        PositionOrder sellOrder = PositionOrder.builder().id(1).broker(SELL_BROKER).timestamp(2).product(PRODUCT_NAME).side(SELL).client(SELL_CLIENT).details(OrderDetails.builder().price(1).amount(1).build()).build();

        BuySellResult.builder().amount(1).price(1).productName(PRODUCT_NAME).buyOrder(buyOrder).sellOrder(sellOrder).build();
    }
}
