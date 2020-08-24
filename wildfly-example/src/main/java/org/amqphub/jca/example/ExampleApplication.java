package org.amqphub.jca.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    BlockingQueue<String> responses = new LinkedBlockingQueue<>();

    @POST
    @Path("/send-request")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public synchronized String sendRequest(String text) {
        log.infof("Sending request message");

        Queue requests = jmsContext.createQueue("example/requests");
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage();

        try {
            message.setText(text);

            producer.send(requests, message);

            return message.getJMSMessageID() + "\n";
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/receive-response")
    @Produces(MediaType.TEXT_PLAIN)
    public String receiveResponse() {
        log.infof("Receiving response message");

        String response;

        try {
            response = responses.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return response + "\n";
    }
}
