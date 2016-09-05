package com.gft.digitalbank.exchange.solution.message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class ConcurrentBlockingHashMap<K, V> extends ConcurrentHashMap<K, V> {
    private final Map<Object, LockConditionPair> lockConditionPairMap = new ConcurrentHashMap<>();

    public ConcurrentBlockingHashMap() {
        super();
    }

    public ConcurrentBlockingHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    private LockConditionPair getLockConditionPair(Object key) {
        LockConditionPair lockConditionPair = lockConditionPairMap.get(key);
        if (lockConditionPair == null) {
            lockConditionPair = new LockConditionPair(new ReentrantLock());
            LockConditionPair existingConditionPair = lockConditionPairMap.putIfAbsent(key, lockConditionPair);
            if (existingConditionPair != null) {
                lockConditionPair = existingConditionPair;
            }
        }
        return lockConditionPair;
    }

    private void waitIfUnavailable(Object key, ConsumerWithInterruptedException<LockConditionPair> lockConditionPairConsumer) throws InterruptedException {
        if (!super.containsKey(key)) {
            LockConditionPair lockConditionPair = getLockConditionPair(key);
            lockConditionPair.lock.lock();
            try {
                if (!lockConditionPair.isAvailable.get()) {
                    lockConditionPairConsumer.accept(lockConditionPair);
                } else {
                    lockConditionPairMap.remove(key);
                }
            } finally {
                lockConditionPairMap.remove(key);
                lockConditionPair.lock.unlock();
            }
        } else {
            lockConditionPairMap.remove(key);
        }
    }

    private void notifyWhenAvailable(Object key) {
        LockConditionPair lockConditionPair = getLockConditionPair(key);
        lockConditionPair.lock.lock();
        try {
            if (!lockConditionPair.isAvailable.get()) {
                lockConditionPair.isAvailable.set(true);
                lockConditionPair.condition.signalAll();
            }
        } finally {
            lockConditionPair.lock.unlock();
        }
    }

    private V doOrBlock(Object key, Function<Object, V> function, ConsumerWithInterruptedException<LockConditionPair> lockConditionPairConsumer) throws InterruptedException {
        waitIfUnavailable(key, lockConditionPairConsumer);
        return function.apply(key);
    }

    public V getOrBlock(Object key) throws InterruptedException {
        return doOrBlock(key, super::get, lockConditionPair -> lockConditionPair.condition.await());
    }

    public V getOrBlock(Object key, long time, TimeUnit timeUnit) throws InterruptedException {
        return doOrBlock(key, super::get, lockConditionPair -> lockConditionPair.condition.await(time, timeUnit));
    }

    public V removeOrBlock(Object key) throws InterruptedException {
        return doOrBlock(key, super::remove, lockConditionPair -> lockConditionPair.condition.await());
    }

    public V removeOrBlock(Object key, long time, TimeUnit timeUnit) throws InterruptedException {
        return doOrBlock(key, super::remove, lockConditionPair -> lockConditionPair.condition.await(time, timeUnit));
    }

    @Override
    public V put(K key, V value) {
        boolean contains = super.containsKey(key);
        V val = super.put(key, value);
        if (!contains) {
            notifyWhenAvailable(key);
        }
        return val;
    }

    @FunctionalInterface
    private interface ConsumerWithInterruptedException<T> {
        void accept(T t) throws InterruptedException;
    }

    private static class LockConditionPair {
        private final Lock lock;
        private final Condition condition;
        private final AtomicBoolean isAvailable;

        LockConditionPair(Lock lock) {
            this.lock = lock;
            this.condition = lock.newCondition();
            this.isAvailable = new AtomicBoolean();
        }
    }
}
