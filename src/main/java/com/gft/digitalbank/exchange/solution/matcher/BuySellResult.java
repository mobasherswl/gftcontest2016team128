package com.gft.digitalbank.exchange.solution.matcher;

import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class BuySellResult {
    static final BuySellResult EMPTY = new BuySellResult();

    private int amount;
    private int price;
    private String productName;
    private PositionOrder buyOrder;
    private PositionOrder sellOrder;
    private boolean buyFullyMatched;
    private boolean sellFullyMatched;

    private BuySellResult() {
    }

    public BuySellResult(int amount, int price, String productName, PositionOrder buyOrder, PositionOrder sellOrder,
                         boolean buyFullyMatched, boolean sellFullyMatched) {
        checkArgument(amount > 0, "amount should be greater then 0");
        checkArgument(price > 0, "price should be greater then 0");
        checkArgument(isNoneBlank(productName), "product name should be provided");
        checkArgument(buyOrder != null, "buyOrder can not be null");
        checkArgument(sellOrder != null, "sellOrder can not be null");
        checkArgument(buyFullyMatched || sellFullyMatched, "at least one order should be fully matched");

        this.amount = amount;
        this.price = price;
        this.productName = productName;
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.buyFullyMatched = buyFullyMatched;
        this.sellFullyMatched = sellFullyMatched;
    }

    public static BuySellResult.BuySellResultBuilder builder() {
        return new BuySellResult.BuySellResultBuilder();
    }

    public int getAmount() {
        return amount;
    }

    public int getPrice() {
        return price;
    }

    public String getProductName() {
        return productName;
    }

    public PositionOrder getBuyOrder() {
        return buyOrder;
    }

    public PositionOrder getSellOrder() {
        return sellOrder;
    }

    boolean isBuyFullyMatched() {
        return buyFullyMatched;
    }

    boolean isSellFullyMatched() {
        return sellFullyMatched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BuySellResult)) {
            return false;
        }

        BuySellResult that = (BuySellResult) o;

        return new EqualsBuilder()
                .append(amount, that.amount)
                .append(price, that.price)
                .append(productName, that.productName)
                .append(buyFullyMatched, that.buyFullyMatched)
                .append(sellFullyMatched, that.sellFullyMatched)
                .append(buyOrder, that.buyOrder)
                .append(sellOrder, that.sellOrder)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(amount)
                .append(price)
                .append(productName)
                .append(buyOrder)
                .append(sellOrder)
                .append(buyFullyMatched)
                .append(sellFullyMatched)
                .toHashCode();
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    public static class BuySellResultBuilder {
        private int amount;
        private int price;
        private String productName;
        private PositionOrder buyOrder;
        private PositionOrder sellOrder;
        private boolean buyFullyMatched;
        private boolean sellFullyMatched;

        BuySellResultBuilder() {
        }

        public BuySellResult.BuySellResultBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public BuySellResult.BuySellResultBuilder price(int price) {
            this.price = price;
            return this;
        }

        public BuySellResult.BuySellResultBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public BuySellResult.BuySellResultBuilder buyOrder(PositionOrder buyOrder) {
            this.buyOrder = buyOrder;
            return this;
        }

        public BuySellResult.BuySellResultBuilder sellOrder(PositionOrder sellOrder) {
            this.sellOrder = sellOrder;
            return this;
        }

        public BuySellResult.BuySellResultBuilder buyFullyMatched(boolean buyFullyMatched) {
            this.buyFullyMatched = buyFullyMatched;
            return this;
        }

        BuySellResult.BuySellResultBuilder sellFullyMatched(boolean sellFullyMatched) {
            this.sellFullyMatched = sellFullyMatched;
            return this;
        }

        public BuySellResult build() {
            return new BuySellResult(this.amount, this.price, this.productName, this.buyOrder, this.sellOrder,
                    this.buyFullyMatched, this.sellFullyMatched);
        }

        @Override
        public String toString() {
            return reflectionToString(BuySellResultBuilder.this);
        }
    }

}
