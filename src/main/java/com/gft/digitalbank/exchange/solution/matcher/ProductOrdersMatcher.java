package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.Side;
import com.gft.digitalbank.exchange.solution.orderbooks.BuySellOrderQueue;
import com.gft.digitalbank.exchange.solution.transactions.TransactionPersistenceService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

import static com.google.common.base.Preconditions.checkArgument;

public class ProductOrdersMatcher implements QueuesMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductOrdersMatcher.class);
    private TransactionPersistenceService transactionService;
    private BuySellPairMatcher pairMatcher;

    @Inject
    ProductOrdersMatcher(BuySellPairMatcher pairMatcher, TransactionPersistenceService transactionService) {

        checkArgument(pairMatcher != null, "BuySellPairMatcher can not be null");
        checkArgument(transactionService != null, "TransactionService can not be null");

        this.transactionService = transactionService;
        this.pairMatcher = pairMatcher;
    }

    @Override
    public void match(BuySellOrderQueue orders) {
        checkArgument(orders != null, "orders add can not be null");
        Queue<PositionOrder> buyOrders = orders.get(Side.BUY);
        Queue<PositionOrder> sellOrders = orders.get(Side.SELL);

        checkArgument(buyOrders != null, "Buy orders can not be null");
        checkArgument(sellOrders != null, "Sell orders can not be null");

        matchEntries(orders, buyOrders, sellOrders);
    }

    private void matchEntries(BuySellOrderQueue orders, Queue<PositionOrder> buyOrders, Queue<PositionOrder> sellOrders) {
        PositionOrder buyOrder = buyOrders.peek();
        PositionOrder sellOrder = sellOrders.peek();

        while (buyOrder != null && sellOrder != null) {
            LOGGER.info("BuyOrder {}, SellOrder {}", buyOrder, sellOrder);
            BuySellResult matchResult = pairMatcher.getMatchResult(buyOrder, sellOrder);
            if (matchResult == BuySellResult.EMPTY) {
                return;
            }
            orders.remove(buyOrder);
            orders.remove(sellOrder);
            transactionService.save(matchResult);
            if (!matchResult.isBuyFullyMatched()) {
                sellOrder = sellOrders.peek();
                buyOrder = createUpdatedOrder(buyOrder, matchResult.getAmount());
                orders.put(buyOrder);
            } else if (!matchResult.isSellFullyMatched()) {
                buyOrder = buyOrders.peek();
                sellOrder = createUpdatedOrder(sellOrder, matchResult.getAmount());
                orders.put(sellOrder);
            } else {
                buyOrder = buyOrders.peek();
                sellOrder = sellOrders.peek();
            }
        }
    }


    private PositionOrder createUpdatedOrder(PositionOrder buyOrder, int amount) {
        return PositionOrder.builder()
                .id(buyOrder.getId())
                .details(OrderDetails.builder().amount(buyOrder.getDetails().getAmount() - amount)
                        .price(buyOrder.getDetails().getPrice()).build())
                .broker(buyOrder.getBroker())
                .client(buyOrder.getClient()).timestamp(buyOrder.getTimestamp())
                .side(buyOrder.getSide())
                .product(buyOrder.getProduct()).build();
    }

}
