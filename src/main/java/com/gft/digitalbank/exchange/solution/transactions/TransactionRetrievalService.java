package com.gft.digitalbank.exchange.solution.transactions;

import com.gft.digitalbank.exchange.model.Transaction;

import java.util.Set;

@FunctionalInterface
public interface TransactionRetrievalService {
    Set<Transaction> retrieveTransactions();
}
