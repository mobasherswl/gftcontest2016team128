package com.gft.digitalbank.exchange.solution.finalizer;

import com.gft.digitalbank.exchange.listener.ProcessingListener;

@FunctionalInterface
public interface FinalizationService {
    void finalizeProcess(ProcessingListener processingListener);
}
