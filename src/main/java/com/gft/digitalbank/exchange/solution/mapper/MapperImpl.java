package com.gft.digitalbank.exchange.solution.mapper;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.google.gson.Gson;

public class MapperImpl implements Mapper<BrokerMessage> {
    Gson mapper = new Gson();

    @Override
    public BrokerMessage fromJson(String message, Class<? extends BrokerMessage> classType) {
        return mapper.fromJson(message, classType);
    }
}