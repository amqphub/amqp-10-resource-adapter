package org.amqphub.jca.example;

import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jboss.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        // @ActivationConfigProperty(propertyName = "user", propertyValue = "example"),
        // @ActivationConfigProperty(propertyName = "password", propertyValue = "example"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672};queue.queue1=example/requests"),
    })
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RequestListener implements MessageListener {
    private static final Logger log = Logger.getLogger(RequestListener.class);

    @Inject
    @JMSConnectionFactory("java:global/jms/default")
    private JMSContext jmsContext;

    @Override
    public void onMessage(Message message) {
        log.infof("Processing request message");

        TextMessage request = (TextMessage) message;
        String responseText;

        try {
            responseText = processRequest(request);
        } catch (Exception e) {
            log.errorf("Processing failed: %s", e.getMessage());
            return;
        }

        Queue responses = jmsContext.createQueue("example/responses");
        JMSProducer producer = jmsContext.createProducer();
        TextMessage response = jmsContext.createTextMessage();

        try {
            response.setJMSCorrelationID(request.getJMSMessageID());
            response.setText(responseText);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        producer.send(responses, response);

        log.infof("Sent response message");
    }

    private String processRequest(TextMessage request) throws Exception {
        String text = request.getText();
        return text.toUpperCase();
    }
}
