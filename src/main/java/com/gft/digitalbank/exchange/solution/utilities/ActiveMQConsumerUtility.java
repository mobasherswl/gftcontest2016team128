package com.gft.digitalbank.exchange.solution.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class ActiveMQConsumerUtility {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQConsumerUtility.class);

    private ActiveMQConsumerUtility() {
    }

    public static void closeResources(Connection connection, Session session, MessageConsumer messageConsumer) {
        closeConsumer(messageConsumer);
        closeSession(session);
        closeConnection(connection);
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            LOG.error("", e);
        }
    }

    public static void closeSession(Session session) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            LOG.error("", e);
        }
    }

    public static void closeConsumer(MessageConsumer messageConsumer) {
        try {
            if (messageConsumer != null) {
                messageConsumer.close();
            }
        } catch (JMSException e) {
            LOG.error("", e);
        }
    }

}