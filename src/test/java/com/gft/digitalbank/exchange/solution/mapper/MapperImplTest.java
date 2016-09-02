package com.gft.digitalbank.exchange.solution.mapper;

import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MapperImplTest {
    @Test
    public void shouldParseMessage() {
        MapperImpl mapper = new MapperImpl();
        mapper.fromJson("{" +
                "\"messageType\": \"SHUTDOWN_NOTIFICATION\"," +
                "\"id\": \"4\"," +
                "\"timestamp\": \"4\"," +
                "\"broker\": \"b001\"}", ShutdownNotification.class);
    }
}