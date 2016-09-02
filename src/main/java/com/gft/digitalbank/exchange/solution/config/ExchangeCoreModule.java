package com.gft.digitalbank.exchange.solution.config;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.solution.consumers.Consumer;
import com.gft.digitalbank.exchange.solution.consumers.ConsumerImpl;
import com.gft.digitalbank.exchange.solution.finalizer.FinalizationService;
import com.gft.digitalbank.exchange.solution.finalizer.FinalizationServiceImpl;
import com.gft.digitalbank.exchange.solution.mapper.Mapper;
import com.gft.digitalbank.exchange.solution.mapper.MapperImpl;
import com.gft.digitalbank.exchange.solution.matcher.ProductOrdersMatcher;
import com.gft.digitalbank.exchange.solution.matcher.QueuesMatcher;
import com.gft.digitalbank.exchange.solution.message.MessageStorage;
import com.gft.digitalbank.exchange.solution.message.OrderedBrokerMessageStorage;
import com.gft.digitalbank.exchange.solution.orderbooks.*;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCache;
import com.gft.digitalbank.exchange.solution.orderbooks.cache.OrderCacheImpl;
import com.gft.digitalbank.exchange.solution.transactions.TransactionPersistenceService;
import com.gft.digitalbank.exchange.solution.transactions.TransactionRetrievalService;
import com.gft.digitalbank.exchange.solution.transactions.TransactionServiceImpl;
import com.gft.digitalbank.exchange.solution.transactions.repositories.InMemoryRepository;
import com.gft.digitalbank.exchange.solution.transactions.repositories.TransactionRepository;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExchangeCoreModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeCoreModule.class);

    @Override
    protected void configure() {
        LOG.info("Guice configuration initializing");
        configureConsumer();
        configureBookProcessor();
        configureTransactions();
        configureMatcher();
        configureFinalizer();
    }

    private void configureConsumer() {
        bind(Consumer.class).to(ConsumerImpl.class);
        bind(new TypeLiteral<Mapper<BrokerMessage>>() {
        }).to(MapperImpl.class);
        bind(new TypeLiteral<MessageStorage<BrokerMessage>>() {
        }).to(OrderedBrokerMessageStorage.class);
    }

    private void configureBookProcessor() {
        bind(OrderCache.class).to(OrderCacheImpl.class);
        bind(OrderProcessor.class).to(OrderProcessorImpl.class);
        bind(OrderBookProcessor.class).toProvider(OrderBookProcessorFactoryImpl.class);
        bind(OrderBookProcessorFactory.class).to(OrderBookProcessorFactoryImpl.class);
    }

    private void configureMatcher() {
        bind(QueuesMatcher.class).to(ProductOrdersMatcher.class);
    }

    private void configureTransactions() {
        bind(TransactionPersistenceService.class).to(TransactionServiceImpl.class);
        bind(TransactionRepository.class).to(InMemoryRepository.class);
    }

    private void configureFinalizer() {
        bind(FinalizationService.class).to(FinalizationServiceImpl.class);
        bind(OrderBookRetrievalService.class).to(OrderProcessorImpl.class);
        bind(TransactionRetrievalService.class).to(TransactionServiceImpl.class);
    }
}
