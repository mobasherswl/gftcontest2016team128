package com.gft.digitalbank.exchange.solution.consumers;

import java.util.List;

@FunctionalInterface
public interface Consumer {
    void initializeConsumers(List<String> destinations);
}
