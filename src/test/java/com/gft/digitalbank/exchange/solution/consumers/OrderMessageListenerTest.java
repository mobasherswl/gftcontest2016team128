package com.gft.digitalbank.exchange.solution.consumers;

import com.gft.digitalbank.exchange.model.orders.ModificationOrder;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.model.orders.ShutdownNotification;
import com.gft.digitalbank.exchange.solution.mapper.MapperImpl;
import com.gft.digitalbank.exchange.solution.message.OrderedBrokerMessageStorage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrderMessageListenerTest {
    @Mock
    TextMessage textMessage;
    @Mock
    ConcurrentHashMap<String, Object> properties;
    @Mock
    ActiveMQTextMessage activeMqTextMessage;
    @Mock
    PositionOrder positionOrder;
    @Mock
    ShutdownNotification shutdownNotification;
    @Mock
    ModificationOrder modificationOrder;
    @Mock
    MapperImpl mapper;
    @Mock
    OrderedBrokerMessageStorage orderedBrokerMessageStorage;
    @InjectMocks
    OrderMessageListener orderMessageListener;

    @Test
    public void shouldNotProcessMessagesOtherThanActiveMQTextMessages() throws InterruptedException {
        orderMessageListener.onMessage(textMessage);
        verify(orderedBrokerMessageStorage, times(0)).add(any());
    }

    @Test

    public void shouldProcessMessage() throws InterruptedException, JMSException, IOException {
        when(activeMqTextMessage.getProperties()).thenReturn(properties);
        when(properties.get("messageType")).thenReturn("ORDER");
        when(activeMqTextMessage.getText()).thenReturn("Sample Message");
        when(mapper.fromJson("Sample Message", PositionOrder.class)).thenReturn(positionOrder);
        doNothing().when(orderedBrokerMessageStorage).add(positionOrder);

        orderMessageListener.onMessage(activeMqTextMessage);
        verify(orderedBrokerMessageStorage, times(1)).add(any());
    }

    @Test

    public void shouldProcessModificationMessage() throws InterruptedException, JMSException, IOException {
        when(activeMqTextMessage.getProperties()).thenReturn(properties);
        when(properties.get("messageType")).thenReturn("MODIFICATION");
        when(activeMqTextMessage.getText()).thenReturn("Sample Message");
        when(mapper.fromJson("Sample Message", ModificationOrder.class)).thenReturn(modificationOrder);
        doNothing().when(orderedBrokerMessageStorage).add(modificationOrder);

        orderMessageListener.onMessage(activeMqTextMessage);
        verify(orderedBrokerMessageStorage, times(1)).add(any());
    }

    @Test
    public void shouldNotSaveMessageIfMessageIsNull() throws InterruptedException, JMSException, IOException {
        when(activeMqTextMessage.getProperties()).thenReturn(properties);
        when(properties.get("messageType")).thenReturn("CANCEL");
        when(activeMqTextMessage.getText()).thenReturn("Sample Message");
        when(mapper.fromJson("Sample Message", PositionOrder.class)).thenReturn(null);
        doNothing().when(orderedBrokerMessageStorage).add(positionOrder);

        orderMessageListener.onMessage(activeMqTextMessage);
        verify(orderedBrokerMessageStorage, times(0)).add(any());
    }

    @Test
    public void shouldCloseResourcesWhenMessageIsShutdown() throws IOException, JMSException {
        when(activeMqTextMessage.getProperties()).thenReturn(properties);
        when(properties.get("messageType")).thenReturn("SHUTDOWN_NOTIFICATION");
        when(activeMqTextMessage.getText()).thenReturn("Sample Message");
        when(mapper.fromJson("Sample Message", ShutdownNotification.class)).thenReturn(shutdownNotification);
        doNothing().when(orderedBrokerMessageStorage).add(shutdownNotification);

        orderMessageListener.onMessage(activeMqTextMessage);
        verify(orderedBrokerMessageStorage, times(1)).add(any());
    }

}
