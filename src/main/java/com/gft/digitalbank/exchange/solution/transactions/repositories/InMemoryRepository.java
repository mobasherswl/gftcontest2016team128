package com.gft.digitalbank.exchange.solution.transactions.repositories;


import com.gft.digitalbank.exchange.model.Transaction;
import com.google.inject.Singleton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class InMemoryRepository implements TransactionRepository {

    private Set<Transaction> transactionContainer = ConcurrentHashMap.newKeySet();

    @Override
    public void save(final Transaction transaction) {
        transactionContainer.add(transaction);
    }

    @Override
    public Set<Transaction> findAll() {
        return transactionContainer;
    }


}
