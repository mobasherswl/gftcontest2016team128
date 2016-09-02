package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.solution.orderbooks.BuySellOrderQueue;

@FunctionalInterface
public interface QueuesMatcher {
    void match(BuySellOrderQueue orders);
}
