package com.gft.digitalbank.exchange.solution.consumers;

import com.gft.digitalbank.exchange.model.orders.BrokerMessage;
import com.gft.digitalbank.exchange.solution.mapper.Mapper;
import com.gft.digitalbank.exchange.solution.message.MessageStorage;
import com.gft.digitalbank.exchange.verification.env.Config;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.List;

@Singleton
public class ConsumerImpl implements Consumer {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerImpl.class);

    private ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Config.getMessagingBindAddress());

    @Inject
    private MessageStorage<BrokerMessage> messageStorage;
    @Inject
    private Mapper<BrokerMessage> mapper;

    @Override
    public void initializeConsumers(List<String> destinations) {
        try {
            LOG.info("initializing consumers from destinations {}", destinations);
            for (String destination : destinations) {
                createConsumer(destination);
            }
        } catch (JMSException e) {
            LOG.error("Unable to initialize consumers with destinations.", e);
        }
    }

    private void createConsumer(String destination) throws JMSException {
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(new ActiveMQQueue(destination));
        OrderMessageListener orderMessageListener = new OrderMessageListener(connection, session, messageConsumer, mapper, messageStorage);
        messageConsumer.setMessageListener(orderMessageListener);
        LOG.debug("consumer created {}", messageConsumer);
        connection.start();
    }

}
