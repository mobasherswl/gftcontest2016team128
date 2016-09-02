package com.gft.digitalbank.exchange.solution.finalizer;

import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.solution.orderbooks.OrderBookRetrievalService;
import com.gft.digitalbank.exchange.solution.transactions.TransactionRetrievalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FinzalizationServiceImplTest {
    @Mock
    ProcessingListener processingListener;
    @Mock
    TransactionRetrievalService transactionRetrievalService;
    @Mock
    OrderBookRetrievalService orderBookRetrievalService;
    @InjectMocks
    FinalizationServiceImpl finalizationServiceImpl;

    @Test
    public void finalizeProcessShouldDoneProcessing() {
        Set<OrderBook> orderBooks = new HashSet<>();
        Set<Transaction> transactions = new HashSet<>();
        SolutionResult result = new SolutionResult(transactions, orderBooks);
        when(orderBookRetrievalService.retrieveOrderBooks()).thenReturn(orderBooks);
        when(transactionRetrievalService.retrieveTransactions()).thenReturn(transactions);

        finalizationServiceImpl.finalizeProcess(processingListener);

        verify(processingListener, times(1)).processingDone(result);
    }

    @Test(expected = RuntimeException.class)
    public void finalizeProcessShouldThrowExceptionInCaseOfNullTransactions() {
        when(orderBookRetrievalService.retrieveOrderBooks()).thenReturn(new HashSet<>());
        when(transactionRetrievalService.retrieveTransactions()).thenReturn(null);

        finalizationServiceImpl.finalizeProcess(processingListener);
    }

    @Test(expected = RuntimeException.class)
    public void finalizeProcessShouldThrowExceptionInCaseOfNullOrderBooks() {
        when(orderBookRetrievalService.retrieveOrderBooks()).thenReturn(null);
        when(transactionRetrievalService.retrieveTransactions()).thenReturn(new HashSet<>());

        finalizationServiceImpl.finalizeProcess(processingListener);
    }

    @Test
    public void finalizeProcessShouldNotThrowExceptionInCaseOfEmptyOrderBooksAndEmptyTransactions() {
        when(orderBookRetrievalService.retrieveOrderBooks()).thenReturn(new HashSet<>());
        when(transactionRetrievalService.retrieveTransactions()).thenReturn(new HashSet<>());

        finalizationServiceImpl.finalizeProcess(processingListener);
    }
}
