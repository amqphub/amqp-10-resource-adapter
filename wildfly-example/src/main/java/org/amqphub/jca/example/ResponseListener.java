package org.amqphub.jca.example;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.jboss.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "user", propertyValue = "example"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "example"),
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://localhost:5672;queue.queue1=example/responses"),
    })
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ResponseListener implements MessageListener {
    private static final Logger log = Logger.getLogger(RequestListener.class);

    @Inject
    private ExampleApplication app;

    @Override
    public void onMessage(Message message) {
        log.infof("Processing response message");

        TextMessage request = (TextMessage) message;
        String text;

        try {
            text = request.getText();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        app.responses.add(text);
    }
}
