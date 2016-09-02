package com.gft.digitalbank.exchange.solution.config;

import com.gft.digitalbank.exchange.solution.matcher.BuySellPairMatcher;
import com.gft.digitalbank.exchange.solution.matcher.ProductOrdersMatcher;
import com.gft.digitalbank.exchange.solution.transactions.TransactionPersistenceService;
import com.google.inject.Injector;
import org.junit.Test;

import static com.google.inject.Guice.createInjector;
import static org.junit.Assert.assertNotNull;

public class ExchangeCoreModuleTest {
    private Injector guiceInjector = createInjector(new ExchangeCoreModule());

    @Test
    public void testIfTransactionServiceIsSuccessfullyCreated() throws Exception {
        assertNotNull(guiceInjector.getInstance(TransactionPersistenceService.class));
    }

    @Test
    public void testIfProductOrdersMatcherIsSuccessfullyCreated() throws Exception {
        assertNotNull(guiceInjector.getInstance(ProductOrdersMatcher.class));
    }

    @Test
    public void testIfBuySellPairMatcherIsSuccessfullyCreated() throws Exception {
        assertNotNull(guiceInjector.getInstance(BuySellPairMatcher.class));
    }
}
