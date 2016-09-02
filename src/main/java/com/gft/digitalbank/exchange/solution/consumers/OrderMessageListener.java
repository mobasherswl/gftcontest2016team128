package com.gft.digitalbank.exchange.solution.consumers;

import com.gft.digitalbank.exchange.model.orders.*;
import com.gft.digitalbank.exchange.solution.mapper.Mapper;
import com.gft.digitalbank.exchange.solution.message.MessageStorage;
import com.gft.digitalbank.exchange.solution.utilities.ActiveMQConsumerUtility;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;

class OrderMessageListener implements MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(OrderMessageListener.class);

    private static final String MESSAGE_TYPE = "messageType";
    private MessageStorage<BrokerMessage> messageStorage;
    private Connection connection;
    private Session session;
    private MessageConsumer messageConsumer;
    private Mapper<BrokerMessage> mapper;

    OrderMessageListener(Connection connection, Session session, MessageConsumer messageConsumer, Mapper<BrokerMessage> mapper, MessageStorage<BrokerMessage> messageStorage) {
        this.connection = connection;
        this.session = session;
        this.messageConsumer = messageConsumer;
        this.mapper = mapper;
        this.messageStorage = messageStorage;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ActiveMQTextMessage) {
                LOG.info(" Thread ID {} , message consumed {} ", Thread.currentThread().getId(), ((ActiveMQTextMessage) message).getText());
                ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
                MessageType type = getTypeOfMessage(activeMQTextMessage);
                if (MessageType.SHUTDOWN_NOTIFICATION.equals(type)) {
                    ActiveMQConsumerUtility.closeResources(connection, session, messageConsumer);
                }

                BrokerMessage brokerMessage = mapMessage(activeMQTextMessage, type);
                if (brokerMessage != null) {
                    messageStorage.add(brokerMessage);
                }

            }
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private MessageType getTypeOfMessage(ActiveMQTextMessage activeMQTextMessage) throws IOException {
        return MessageType.valueOf((String) activeMQTextMessage.getProperties().get(MESSAGE_TYPE));
    }

    private BrokerMessage mapMessage(ActiveMQTextMessage activeMQTextMessage, MessageType type) {
        BrokerMessage brokerMessage = null;
        try {
            if (type != null) {
                switch (type) {
                    case ORDER:
                        brokerMessage = mapper.fromJson(activeMQTextMessage.getText(), PositionOrder.class);
                        break;
                    case CANCEL:
                        brokerMessage = mapper.fromJson(activeMQTextMessage.getText(), CancellationOrder.class);
                        break;
                    case MODIFICATION:
                        brokerMessage = mapper.fromJson(activeMQTextMessage.getText(), ModificationOrder.class);
                        break;
                    case SHUTDOWN_NOTIFICATION:
                        brokerMessage = mapper.fromJson(activeMQTextMessage.getText(), ShutdownNotification.class);
                        break;
                    default:
                        break;
                }
            }
        } catch (JMSException e) {
            LOG.error("", e);
        }
        return brokerMessage;
    }

}
