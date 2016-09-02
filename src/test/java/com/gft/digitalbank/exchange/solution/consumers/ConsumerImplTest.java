package com.gft.digitalbank.exchange.solution.consumers;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerImplTest {

    @Mock
    ActiveMQConnectionFactory activeMQConnectionFactory;
    @Mock
    Connection connection;
    @Mock
    Session session;
    @Mock
    MessageConsumer messageConsumer;
    @InjectMocks
    ConsumerImpl consumerImpl;

    @Test
    public void shouldCreateConsumersForDestinations() throws JMSException {
        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createConsumer(any(ActiveMQDestination.class))).thenReturn(messageConsumer);

        doNothing().when(connection).start();

        List<String> destinations = new ArrayList<>();
        destinations.add("Dummy");
        consumerImpl.initializeConsumers(destinations);
    }

}