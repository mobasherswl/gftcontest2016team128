package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;

import static com.gft.digitalbank.exchange.solution.matcher.BuySellResult.EMPTY;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.min;

public class BuySellPairMatcher {

    BuySellResult getMatchResult(PositionOrder buyOrder, PositionOrder sellOrder) {
        checkArgument(buyOrder != null, "buy order can not be null");
        checkArgument(sellOrder != null, "sell order can not be null");

        return buyOrder.getDetails().getPrice() >= sellOrder.getDetails().getPrice()
                ? createResult(min(buyOrder.getDetails().getAmount(), sellOrder.getDetails().getAmount()),
                getPriceOfOrderWithLowestTimestamp(buyOrder, sellOrder), buyOrder, sellOrder)
                : EMPTY;
    }

    private BuySellResult createResult(int amount, int price, PositionOrder buyEntry, PositionOrder sellEntry) {
        return BuySellResult.builder()
                .amount(amount)
                .price(price)
                .productName(buyEntry.getProduct())
                .buyOrder(buyEntry)
                .sellOrder(sellEntry)
                .buyFullyMatched(buyEntry.getDetails().getAmount() - amount == 0)
                .sellFullyMatched(sellEntry.getDetails().getAmount() - amount == 0)
                .build();
    }

    private int getPriceOfOrderWithLowestTimestamp(PositionOrder buyOrder, PositionOrder sellOrder) {
        return buyOrder.getTimestamp() <= sellOrder.getTimestamp()
                ? buyOrder.getDetails().getPrice()
                : sellOrder.getDetails().getPrice();
    }
}
