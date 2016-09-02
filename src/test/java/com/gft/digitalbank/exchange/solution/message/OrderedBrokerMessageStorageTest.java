package com.gft.digitalbank.exchange.solution.message;

import com.gft.digitalbank.exchange.model.orders.*;
import com.gft.digitalbank.exchange.solution.test.Holder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gft.digitalbank.exchange.solution.test.OrderMessageUtils.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class OrderedBrokerMessageStorageTest {
    private final String BROKER_SHUTDOWN = "shutdown";
    private MessageStorage<BrokerMessage> storage;
    private Executor executor = Executors.newFixedThreadPool(3);

    private static final PositionOrder POSITION_ORDER_1 = createPositionOrder(1);
    private static final ModificationOrder MODIFICATION_ORDER_2 = createModificationOrder(2);
    private static final ModificationOrder MODIFICATION_ORDER_3 = createModificationOrder(3);
    private static final ShutdownNotification SHUTDOWN_NOTIFICATION = SHUTDOWN_1;

    @Before
    public void setUp() throws Exception {
        storage = new OrderedBrokerMessageStorage();
    }

    @Test
    public void shouldDequeueMessageWithIdAscendingOrderWhenTwoMessagesQueuedInOrder() throws Exception {
        storage.add(POSITION_ORDER_1);
        storage.add(MODIFICATION_ORDER_2);

        BrokerMessage result = storage.get();
        assertEquals(POSITION_ORDER_1, result);
    }

    @Test
    public void shouldDequeueMessageWithIdAscendingOrderWhenTwoMessagesQueuedInReversedOrder() throws Exception {
        storage.add(MODIFICATION_ORDER_2);
        storage.add(POSITION_ORDER_1);

        BrokerMessage result = storage.get();

        assertEquals(POSITION_ORDER_1, result);
    }

    @Test
    public void shouldDequeueMessageWithIdAscendingOrderWhenTwoMessagesAndShutdown() throws Exception {
        storage.add(SHUTDOWN_NOTIFICATION);
        storage.add(MODIFICATION_ORDER_2);
        storage.add(POSITION_ORDER_1);

        assertEquals(POSITION_ORDER_1, storage.get());
        assertEquals(MODIFICATION_ORDER_2, storage.get());
        assertEquals(SHUTDOWN_NOTIFICATION, storage.get());
    }

    @Test
    public void shouldCheckOneThreadQueuesAndOneThreadDequeuesMessageWithIdAscendingOrder() throws Throwable {
        final Holder<Throwable> holder = new Holder<>();
        Integer messagesCount = 4;
        AtomicInteger receivedCount = new AtomicInteger(0);
        BrokerMessage[] expected = new BrokerMessage[]{POSITION_ORDER_1, MODIFICATION_ORDER_2, MODIFICATION_ORDER_3, SHUTDOWN_NOTIFICATION};
        BrokerMessage[] received = new BrokerMessage[messagesCount];

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                storage.add(MODIFICATION_ORDER_2);
                storage.add(MODIFICATION_ORDER_3);
                storage.add(POSITION_ORDER_1);
                storage.add(SHUTDOWN_NOTIFICATION);
            }
        };

        CompletableFuture<Void> dequeueClientFutureResult = CompletableFuture.runAsync(() -> {
            BrokerMessage first;
            while (receivedCount.get() < messagesCount) {
                first = storage.get();
                if (first != null) {
                    received[receivedCount.getAndIncrement()] = first;
                }
            }
            assertArrayEquals(expected, received);
        }).exceptionally(throwable -> {
            holder.object = throwable.getCause();
            return null;
        });

        thread1.start();
        dequeueClientFutureResult.get(2, TimeUnit.SECONDS);

        if (holder.object != null) {
            throw holder.object;
        }
    }

    @Test
    public void shouldCheckTwoThreadsQueuePositionOrdersWithDelaysAndOneThreadDequeuesMessages() throws Throwable {
        final Holder<Throwable> holder = new Holder<>();
        final AtomicInteger currentId = new AtomicInteger(1);
        final AtomicInteger resultId = new AtomicInteger(1);
        final AtomicInteger shutdownsCount = new AtomicInteger(2);
        final Random sleepRandom = new Random(556);
        final Integer MAX_ID = 30;
        final ShutdownNotification SHUTDOWN_1 = createShutdownNotification(1, BROKER_SHUTDOWN);
        final ShutdownNotification SHUTDOWN_2 = createShutdownNotification(1, BROKER_SHUTDOWN);

        Runnable thread1 = () -> {
            int id = currentId.getAndIncrement();
            while (id < MAX_ID) {
                storage.add(createPositionOrder(id, BROKER_1));
                id = currentId.getAndIncrement();
            }
            storage.add(SHUTDOWN_1);
        };

        Runnable thread2 = () -> {
            int id = currentId.getAndIncrement();
            while (id < MAX_ID) {
                storage.add(createPositionOrder(id, BROKER_2));
                id = currentId.getAndIncrement();
            }
            storage.add(SHUTDOWN_2);
        };

        CompletableFuture<Void> dequeueClientFutureResult = CompletableFuture.runAsync(() -> {
            BrokerMessage message;
            while (resultId.get() < MAX_ID || shutdownsCount.get() > 0) {
                message = storage.get();
                if (message != null) {
                    if (message.getMessageType() == MessageType.SHUTDOWN_NOTIFICATION) {
                        assertEquals(message.getId() == 1 ? SHUTDOWN_1 : SHUTDOWN_2, message);
                        shutdownsCount.decrementAndGet();
                    } else {
                        assertEquals(resultId.getAndIncrement(), message.getId());
                    }
                }
            }
        }, executor).exceptionally(throwable -> {
            holder.object = throwable.getCause();
            return null;
        });

        executor.execute(thread1);
        executor.execute(thread2);
        dequeueClientFutureResult.get();

        if (holder.object != null) {
            throw holder.object;
        }
    }
}
