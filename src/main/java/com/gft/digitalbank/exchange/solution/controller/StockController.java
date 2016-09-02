package com.gft.digitalbank.exchange.solution.controller;

import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.solution.consumers.Consumer;
import com.gft.digitalbank.exchange.solution.finalizer.FinalizationService;
import com.gft.digitalbank.exchange.solution.message.MessageStorage;
import com.gft.digitalbank.exchange.solution.orderbooks.OrderProcessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@Singleton
public class StockController {
    private static final Logger LOG = LoggerFactory.getLogger(StockController.class);
    private final MessageStorage<BrokerMessage> brokerMessageStorage;
    private final OrderProcessor orderProcessor;
    private final FinalizationService finalizationService;
    private int brokersIdle;
    private ProcessingListener processingListener;
    private List<String> destinations;
    private Consumer consumerService;

    @Inject
    StockController(OrderProcessor orderProcessor,
                    FinalizationService finalizationService,
                    MessageStorage<BrokerMessage> brokerMessageStorage,
                    Consumer consumerService) {
        checkArgument(orderProcessor != null, "order processor cannot be null");
        checkArgument(finalizationService != null, "finalizationService cannot be null");
        checkArgument(brokerMessageStorage != null, "message storage cannot be null");
        checkArgument(consumerService != null, "consumerService cannot be null");

        this.orderProcessor = orderProcessor;
        this.finalizationService = finalizationService;
        this.brokerMessageStorage = brokerMessageStorage;
        this.consumerService = consumerService;
    }

    public void start() {
        checkState(processingListener != null, "processing listener is not set");

        LOG.info("Start processing messages");
        consumerService.initializeConsumers(destinations);
        Executors.newSingleThreadExecutor().execute(() -> {
            while (brokersIdle > 0) {
                BrokerMessage brokerMessage = brokerMessageStorage.get();
                if (brokerMessage != null) {
                    process(brokerMessage);
                }
            }
            LOG.info("System is shutting down");
            finalizationService.finalizeProcess(processingListener);
        });
    }

    public void setProcessingListener(ProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    public void setDestinations(List<String> brokers) {
        if (brokers != null && !brokers.isEmpty()) {
            destinations = brokers;
            brokersIdle = brokers.size();
        } else {
            throw new IllegalArgumentException("destinations can not be null or empty");
        }
    }

    private void process(BrokerMessage message) {
        switch (message.getMessageType()) {
            case ORDER:
            case MODIFICATION:
            case CANCEL:
                orderProcessor.process(message);
                break;
            case SHUTDOWN_NOTIFICATION:
                brokersIdle--;
                break;
            default:
        }
    }

}
