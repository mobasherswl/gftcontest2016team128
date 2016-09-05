package com.gft.digitalbank.exchange.solution;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.solution.config.ExchangeCoreModule;
import com.gft.digitalbank.exchange.solution.controller.StockController;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.List;

import static com.google.inject.Guice.createInjector;

/**
 * Your solution must implement the {@link Exchange} interface.
 */
public class StockExchange implements Exchange {
    private Injector guiceInjector = createInjector(new ExchangeCoreModule());
    private StockController stockController = guiceInjector.getInstance(StockController.class);

    @Override
    public void register(ProcessingListener processingListener) {
        stockController.setProcessingListener(processingListener);
    }

    @Override
    public void setDestinations(List<String> list) {
        stockController.setDestinations(list);
    }

    @Override
    public void start() {
        stockController.start();
    }
}
