package com.gft.digitalbank.exchange.solution.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class ActiveMQConsumerUtilityTest {

    @Mock
    Connection connection;
    @Mock
    Session session;
    @Mock
    MessageConsumer consumer;
    @InjectMocks
    ActiveMQConsumerUtility activeMQConsumerUtility;

    @Test
    public void shouldCloseResourcesWhenNotNull() {
        ActiveMQConsumerUtility.closeResources(connection, session, consumer);
    }

    @Test
    public void shouldCloseConnectionWhenNotNull() {
        ActiveMQConsumerUtility.closeConnection(connection);
    }

    @Test
    public void shouldThrowExceptionWhenConnectionNotClosed() throws JMSException {
        doThrow(JMSException.class).when(connection).close();
        ActiveMQConsumerUtility.closeConnection(connection);
    }

    @Test
    public void shouldCloseSessionWhenNotNull() {
        ActiveMQConsumerUtility.closeSession(session);
    }

    @Test
    public void shouldThrowExceptionWhenSessionNotClosed() throws JMSException {
        doThrow(JMSException.class).when(session).close();
        ActiveMQConsumerUtility.closeSession(session);


    }

    @Test
    public void shouldCloseConsumerWhenNotNull() {
        ActiveMQConsumerUtility.closeConsumer(consumer);
    }

    @Test
    public void shouldThrowExceptionWhenConsumerNotClosed() throws JMSException {
        doThrow(JMSException.class).when(consumer).close();
        ActiveMQConsumerUtility.closeConsumer(consumer);
    }

}