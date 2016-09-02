package com.gft.digitalbank.exchange.solution.transactions;

import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.solution.matcher.BuySellResult;
import com.gft.digitalbank.exchange.solution.transactions.repositories.TransactionRepository;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Singleton
public class TransactionServiceImpl implements TransactionPersistenceService, TransactionRetrievalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final Map<String, AtomicInteger> productTransactionIdMap;

    private final TransactionRepository repository;

    @Inject
    public TransactionServiceImpl(TransactionRepository repository) {
        this.productTransactionIdMap = new ConcurrentHashMap<>();
        this.repository = repository;
    }

    @Override
    public void save(final BuySellResult result) {
        LOGGER.info("BuySellResult received {}", result);
        validateBuySellResult(result);
        repository.save(populateTransaction(result));
    }

    private void validateBuySellResult(final BuySellResult result) {
        Preconditions.checkArgument(result != null, "BuySellResult should not be null");
        Preconditions.checkArgument(result.getAmount() > 0, "Amount should be greater than 0");
        Preconditions.checkArgument(result.getPrice() > 0, "Price should be greater than 0");
        Preconditions.checkArgument(isNotBlank(result.getProductName()), "Product name should not be blank");
    }

    private int getTransactionId(String product) {
        AtomicInteger atomicInteger = productTransactionIdMap.get(product);
        if (atomicInteger == null) {
            atomicInteger = new AtomicInteger(0);
            productTransactionIdMap.put(product, atomicInteger);
        }
        return atomicInteger.incrementAndGet();
    }

    private Transaction populateTransaction(final BuySellResult result) {
        return Transaction.builder()
                .id(getTransactionId(result.getProductName()))
                .amount(result.getAmount())
                .price(result.getPrice())
                .product(result.getProductName())
                .brokerBuy(result.getBuyOrder().getBroker())
                .clientBuy(result.getBuyOrder().getClient())
                .brokerSell(result.getSellOrder().getBroker())
                .clientSell(result.getSellOrder().getClient())
                .build();
    }


    @Override
    public Set<Transaction> retrieveTransactions() {
        LOGGER.info("Transaction set retrieval called");
        return repository.findAll();
    }

}
