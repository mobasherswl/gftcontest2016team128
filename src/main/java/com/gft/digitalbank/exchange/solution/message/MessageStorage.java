package com.gft.digitalbank.exchange.solution.message;

public interface MessageStorage<T> {
    void add(T message);

    T get();
}
