package org.amqphub.jca.example;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Singleton
@ApplicationPath("/api")
@Path("/")
public class ExampleApplication extends Application {
    private static final Logger log = Logger.getLogger(ExampleApplication.class);

    @Inject
    @JMSConnectionFactory("java:global/jms/default")
    private JMSContext jmsContext;

    @POST
    @Path("send-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String sendRequest(Request request) {
        log.infof("Sending request message");

        Queue requests = jmsContext.createQueue("example/requests");
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage();

        try {
            message.setText(request.getText());

            producer.send(requests, message);

            return message.getJMSMessageID() + "\n";
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("receive-response")
    @Produces(MediaType.TEXT_PLAIN)
    public String receiveResponse() {
        log.infof("Receiving response message");

        Queue responses = jmsContext.createQueue("example/responses");
        JMSConsumer consumer = jmsContext.createConsumer(responses);

        TextMessage message = (TextMessage) consumer.receiveNoWait();

        if (message == null) {
            return "[No responses]\n";
        }

        try {
            return message.getJMSCorrelationID() + ": " + message.getText() + "\n";
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
