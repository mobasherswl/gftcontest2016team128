package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static com.gft.digitalbank.exchange.solution.matcher.MatcherTestHelper.*;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class BuySellPairMatcherTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private BuySellPairMatcher buySellOrderPairsMatcher = new BuySellPairMatcher();

    public Object[] buySellOrderPairs() {
        return new Object[][]{
                {createBuyOrder(10, 5), createSellOrder(10, 10), BuySellResult.EMPTY},
                getBuySellOrderAndResult(createBuyOrder(10, 10), createSellOrder(10, 5), 10, 10, true, true),
                getBuySellOrderAndResult(createBuyOrder(5, 20), createSellOrder(10, 20), 5, 20, true, false),
                getBuySellOrderAndResult(createBuyOrder(10, 20), createSellOrder(5, 20), 5, 20, false, true),
                getBuySellOrderAndResult(createBuyOrder(10, 50), createSellOrder(10, 50), 10, 50, true, true),
                getBuySellOrderAndResult(createBuyOrder(10, 30), createSellOrder(7, 30), 7, 30, false, true),
        };
    }

    public Object[] buySellOrderInvalidValue() {
        return new Object[][]{
                {null, createSellOrder(10, 10), IllegalArgumentException.class},
                {createBuyOrder(10, 5), null, IllegalArgumentException.class}
        };
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultCountCorrect(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.getAmount(), result.getAmount());
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultContainsProductNameProperty(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.getProductName(), result.getProductName());
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultPriceCorrect(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.getPrice(), result.getPrice());
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultContainsBuyOrder(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.getBuyOrder(), result.getBuyOrder());
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultContainsSellOrder(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.getSellOrder(), result.getSellOrder());
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultContainsIsBuyFullyMatchedProperty(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.isBuyFullyMatched(), result.isBuyFullyMatched());
    }

    @Test
    @Parameters(method = "buySellOrderPairs")
    public void shouldCheckBuySellOrdersMatchingResultContainsIsSellFullyMatchedProperty(PositionOrder buyOrder, PositionOrder sellOrder, BuySellResult expectedResult) throws Exception {
        BuySellResult result = buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);

        assertEquals(expectedResult.isSellFullyMatched(), result.isSellFullyMatched());
    }

    @Test
    @Parameters(method = "buySellOrderInvalidValue")
    public void shouldThrowExceptionWhenBuyOrderIsNull(PositionOrder buyOrder, PositionOrder sellOrder, Class<Exception> expected) throws Exception {
        expectedException.expect(expected);

        buySellOrderPairsMatcher.getMatchResult(buyOrder, sellOrder);
    }
}
