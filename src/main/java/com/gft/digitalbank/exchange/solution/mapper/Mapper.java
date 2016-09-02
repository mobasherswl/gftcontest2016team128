package com.gft.digitalbank.exchange.solution.mapper;

@FunctionalInterface
public interface Mapper<T> {
    T fromJson(String message, Class<? extends T> classType);
}
