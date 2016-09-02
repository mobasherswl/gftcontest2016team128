package com.gft.digitalbank.exchange.solution.transactions;

import com.gft.digitalbank.exchange.solution.matcher.BuySellResult;

@FunctionalInterface
public interface TransactionPersistenceService {
    void save(BuySellResult result);
}
