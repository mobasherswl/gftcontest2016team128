package com.gft.digitalbank.exchange.solution.transactions.repositories;

import com.gft.digitalbank.exchange.model.Transaction;

import java.util.Set;

public interface TransactionRepository {

    void save(Transaction transaction);

    Set<Transaction> findAll();
}
