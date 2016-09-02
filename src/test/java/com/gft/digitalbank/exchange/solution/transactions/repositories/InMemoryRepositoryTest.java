package com.gft.digitalbank.exchange.solution.transactions.repositories;

import com.gft.digitalbank.exchange.model.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryRepositoryTest {

    @InjectMocks
    InMemoryRepository inMemoryRepository;

    @Test
    public void canStoreTransactions() {
        Transaction transaction = mock(Transaction.class);
        inMemoryRepository.save(transaction);
    }

    @Test
    public void shouldNotReturnNullTransactionSet() {
        assertNotNull(inMemoryRepository.findAll());
    }
}