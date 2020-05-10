# AMQP 1.0 resource adapter

A JCA resource adapter for using AMQP 1.0 messaging with app servers
such as WildFly or Thorntail.

This component combines the
[Generic JMS JCA resource adapter](https://github.com/jms-ra/generic-jms-ra)
with the
[Apache Qpid JMS client](https://qpid.apache.org/components/jms/index.html).

## Maven coordinates

    <dependency>
      <groupId>org.amqphub.jca</groupId>
      <artifactId>resource-adapter</artifactId>
      <version>${current-version}</version>
      <type>rar</type>
    </dependency>

## Example Thorntail configuration

    swarm:
      deployment:
        org.amqphub.jca:resource-adapter.rar:
      resource-adapters:
        resource-adapters:
          default:
            archive: resource-adapter.rar
            transaction-support: NoTransaction
            connection-definitions:
              default:
                jndi-name: java:global/jms/default
                class-name: org.jboss.resource.adapter.jms.JmsManagedConnectionFactory
                config-properties:
                  ConnectionFactory:
                    value: factory1
                  UserName:
                    value: example
                  Password:
                    value: example
                  JndiParameters:
                    value: "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672}"
      ejb3:
        default-resource-adapter-name: default
      ee:
        annotation-property-replacement: true

## Example MDB configuration

```java
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "example"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "example"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://localhost:5672;queue.queue1=example"),
    })
@ResourceAdapter("resource-adapter.rar")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ExampleListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        System.out.println("Received message " + message);
    }
}
```

## Example JMSContext injection

```java
@Singleton
public class ExampleApplication {
    @Inject
    @JMSConnectionFactory("java:global/jms/default")
    private JMSContext jmsContext;

    public synchronized void sendMessage(String text) throws JMSException {
        Queue queue = jmsContext.createQueue("example");
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage();

        message.setText(text);
        producer.send(queue, message);

        System.out.println("Sent message " + message);
    }
}
```

## Running the WildFly example

    $ mvn clean package
    $ ./run-example.sh

    $ curl -X POST -d '{"text": "hello"}' -H 'Content-Type: application/json' http://localhost:8080/wildfly-example/api/send-request && echo
    $ curl -X POST http://localhost:8080/wildfly-example/api/receive-response && echo


<!-- # AMQP 1.0 resource adapter - Thorntail example -->

<!-- 1. Start an AMQP 1.0 server on localhost and port 5672.  If your -->
<!--    server does not create queues on demand, use the tools for your -->
<!--    server to create queues called `example/requests` and -->
<!--    `example/responses`. -->

<!-- 2. Build and run the example code. -->

<!--         $ mvn thorntail:run -->

<!-- 3. In another shell, use `curl` to send text to the `send-request` endpoint. -->

<!--         $ curl -X POST -d '{"text": "hello"}' -H 'Content-Type: application/json' http://localhost:8080/api/send-request -->
<!--         ID:4a63adc0-547c-4881-bc3e-3c8eb7007648:2:1:1-1 -->

<!-- 4. Use `curl` again to get the response from the `receive-response` endpoint. -->

<!--         $ curl -X POST http://localhost:8080/api/receive-response -->
<!--         ID:4a63adc0-547c-4881-bc3e-3c8eb7007648:2:1:1-1: HELLO -->
