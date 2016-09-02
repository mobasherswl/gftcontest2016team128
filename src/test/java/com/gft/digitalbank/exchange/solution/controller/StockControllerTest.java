package com.gft.digitalbank.exchange.solution.controller;


import com.gft.digitalbank.exchange.listener.CountDownLatchProcessingListener;
import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.consumers.Consumer;
import com.gft.digitalbank.exchange.solution.finalizer.FinalizationService;
import com.gft.digitalbank.exchange.solution.message.MessageStorage;
import com.gft.digitalbank.exchange.solution.orderbooks.OrderProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.gft.digitalbank.exchange.solution.test.OrderMessageUtils.*;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class StockControllerTest {
    private PositionOrder POS_ORDER_1 = createPositionOrder(1, BROKER_1);
    private PositionOrder POS_ORDER_2 = createPositionOrder(2, BROKER_2);
    private PositionOrder POS_ORDER_3 = createPositionOrder(3, BROKER_1);
    private ModificationOrder MODIFY_ORDER_4 = createModificationOrder(4);
    private ShutdownNotification SHUTDOWN_5 = createShutdownNotification(5, BROKER_1);
    private ShutdownNotification SHUTDOWN_6 = createShutdownNotification(6, BROKER_2);
    private PositionOrder POS_ORDER_5 = createPositionOrder(5, BROKER_2);

    private MessageStorage<BrokerMessage> EMPTY_STORAGE = mock(MessageStorage.class);
    private MessageStorage<BrokerMessage> mockStorage = mock(MessageStorage.class);
    private OrderProcessor orderProcessor = mock(OrderProcessor.class);
    private FinalizationService finalizationService = mock(FinalizationService.class);

    @Before
    public void setUp() throws Exception {
        reset(mockStorage, orderProcessor, finalizationService);
    }

    @Test
    public void shouldCheckFinalizerReceivedShutdownMessageWhenAllBrokersShutdown() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        when(mockStorage.get()).thenReturn(POS_ORDER_1, POS_ORDER_2, POS_ORDER_3, MODIFY_ORDER_4, POS_ORDER_5, SHUTDOWN_5, SHUTDOWN_6, null);
        StockController stockController = createController(orderProcessor, processingListener1 -> countDownLatch.countDown(), mockStorage);

        stockController.start();

        countDownLatch.await(2, TimeUnit.SECONDS);
        verify(orderProcessor, times(1)).process(POS_ORDER_1);
        verify(orderProcessor, times(1)).process(POS_ORDER_2);
        verify(orderProcessor, times(1)).process(POS_ORDER_3);
        verify(orderProcessor, times(1)).process(MODIFY_ORDER_4);
        verify(orderProcessor, times(1)).process(POS_ORDER_5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenBookProcessorIsNull() throws Exception {
        createController(null, finalizationService, EMPTY_STORAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenFinalizerIsNull() throws Exception {
        createController(orderProcessor, null, EMPTY_STORAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenMessageStorageIsNull() throws Exception {
        createController(orderProcessor, finalizationService, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenConsumerServiceIsNull() throws Exception {
        createController(orderProcessor, finalizationService, EMPTY_STORAGE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenBrokersDestinationsIsEmpty() throws Exception {
        StockController stockController = createController(orderProcessor, finalizationService, EMPTY_STORAGE);
        stockController.setDestinations(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenBrokersDestinationsIsNull() throws Exception {
        StockController stockController = createController(orderProcessor, finalizationService, EMPTY_STORAGE);
        stockController.setDestinations(null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenBrokersProcessingListenerIsNull() throws Exception {
        StockController stockController = createController(orderProcessor, finalizationService, EMPTY_STORAGE);
        stockController.setProcessingListener(null);
        stockController.start();
    }


    private StockController createController(OrderProcessor orderProcessor, FinalizationService finalizationService,
                                             MessageStorage<BrokerMessage> storage) {
        return createController(orderProcessor, finalizationService, storage, mock(Consumer.class));
    }

    private StockController createController(OrderProcessor orderProcessor, FinalizationService finalizationService,
                                             MessageStorage<BrokerMessage> storage, Consumer consumerService) {
        StockController stockController = new StockController(orderProcessor, finalizationService, storage, consumerService);
        stockController.setDestinations(asList(BROKER_1, BROKER_2));
        stockController.setProcessingListener(mock(CountDownLatchProcessingListener.class));
        return stockController;
    }
}
