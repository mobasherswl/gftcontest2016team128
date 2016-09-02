package com.gft.digitalbank.exchange.solution.finalizer;

import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.solution.orderbooks.OrderBookRetrievalService;
import com.gft.digitalbank.exchange.solution.transactions.TransactionRetrievalService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FinalizationServiceImpl implements FinalizationService {
    private TransactionRetrievalService transactionRetrievalService;
    private OrderBookRetrievalService orderBookRetrievalService;

    @Inject
    public FinalizationServiceImpl(TransactionRetrievalService transactionRetrievalService, OrderBookRetrievalService orderBookRetrievalService) {
        this.transactionRetrievalService = transactionRetrievalService;
        this.orderBookRetrievalService = orderBookRetrievalService;
    }

    @Override
    public void finalizeProcess(ProcessingListener processingListener) {
        processingListener.processingDone(SolutionResult.builder()
                .orderBooks(orderBookRetrievalService.retrieveOrderBooks())
                .transactions(transactionRetrievalService.retrieveTransactions())
                .build());
    }

}
