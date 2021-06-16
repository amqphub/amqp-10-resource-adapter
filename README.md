# AMQP 1.0 resource adapter

[![main](https://github.com/amqphub/amqp-10-resource-adapter/actions/workflows/main.yaml/badge.svg)](https://github.com/amqphub/amqp-10-resource-adapter/actions/workflows/main.yaml)

A JCA resource adapter for using AMQP 1.0 messaging with Java app
servers such as WildFly.

This component combines the
[Generic JMS JCA resource adapter](https://github.com/jms-ra/generic-jms-ra)
with the
[Apache Qpid JMS client](https://qpid.apache.org/components/jms/index.html).

Note that this resource adapter does not support distributed
transactions (XA transactions).  It supports only local transactions
and non-transactional messaging.

## Maven coordinates

To use the resource adapter in your project, add the following
dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>org.amqphub.jca</groupId>
  <artifactId>resource-adapter</artifactId>
  <version>${current-version}</version>
  <type>rar</type>
</dependency>
```

## Building the code

The project requires Maven 3.  To build the code, use the `mvn
install` command.

```sh
$ mvn install
```

## Running the example

1. [Configure your WildFly installation.](#wildfly-configuration)

1. [Build the code.](#building-the-code)

1. Copy the `resource-adapter/target/resource-adapter-<version>.rar`
   and `wildfly-example/target/wildfly-example-<version>.war` files to
   the WildFly `standalone/deployments` directory.

1. Copy the `wildfly-example/standalone-custom.xml` file to the
   WildFly `standalone/configuration` directory.

1. In a separate terminal window, start an AMQP 1.0 server with user
   `example` and password `example` on localhost and port 5672.  If
   your server does not create queues on demand, use the tools for
   your server to create queues called `example/requests` and
   `example/responses`.  See `scripts/run-artemis.sh`.

1. In a separate terminal window, start WildFly.  Tell it to use the
   `standalone-custom.xml` configuration file.  See
   `scripts/run-wildfly.sh`.

1. Use `curl` to send text to the `send-request` endpoint.

    ```sh
    $ curl -fX POST http://localhost:8080/wildfly-example/api/send-request -H "content-type: text/plain" -d "hellooo"
    ID:4a63adc0-547c-4881-bc3e-3c8eb7007648:2:1:1-1
    ```

1. Use `curl` again to get the response from the `receive-response` endpoint.

    ```sh
    $ curl -fX POST http://localhost:8080/wildfly-example/api/receive-response
    ID:4a63adc0-547c-4881-bc3e-3c8eb7007648:2:1:1-1: HELLOOO
    ```

The example test performs the steps above.  You can run it with the
following commands:

```sh
$ mvn clean package
$ scripts/test-example.sh
```

## WildFly configuration

Add or modify the `resource-adapters` subsystem.  Change the JNDI and
connection properties according to your needs.

```xml
<subsystem xmlns="urn:jboss:domain:resource-adapters:5.0">
  <resource-adapters>
    <resource-adapter>
      <archive>resource-adapter.rar</archive>
      <transaction-support>NoTransaction</transaction-support>
      <connection-definitions>
        <connection-definition class-name="org.jboss.resource.adapter.jms.JmsManagedConnectionFactory"
                               jndi-name="java:global/jms/default">
          <config-property name="UserName">example</config-property>
          <config-property name="Password">example</config-property>
          <config-property name="ConnectionFactory">factory1</config-property>
          <config-property name="JndiParameters">java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://localhost:5672</config-property>
        </connection-definition>
      </connection-definitions>
    </resource-adapter>
  </resource-adapters>
</subsystem>
```

Add or modify the `ejb3` subsystem.

```xml
<subsystem xmlns="urn:jboss:domain:ejb3:6.0">
  <mdb>
    <resource-adapter-ref resource-adapter-name="resource-adapter.rar"/>
    <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
  </mdb>
  ...
</subsystem>
```

For a complete example, see
[standalone-custom.xml](wildfly-example/standalone-custom.xml).

Additional notes:

* Your WildFly configuration must have the `messaging-activemq`
  subsystem installed, even though you are not using the internal
  broker in this case.

* Your application code must have a
  [`src/main/resources/META-INF/beans.xml`](wildfly-example/src/main/resources/META-INF/beans.xml)
  file that enables bean discovery mode.

* You application code must have a
  [`src/main/resources/META-INF/MANIFEST.MF`](wildfly-example/src/main/resources/META-INF/MANIFEST.MF)
  with an entry that corresponds to the name of your `.rar` file.

## MDB configuration

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

For complete examples, see
[RequestListener.java](wildfly-example/src/main/java/org/amqphub/jca/example/RequestListener.java)
and
[ResponseListener.java](wildfly-example/src/main/java/org/amqphub/jca/example/ResponseListener.java).

## JMSContext injection

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

For a complete example, see
[ExampleApplication.java](wildfly-example/src/main/java/org/amqphub/jca/example/ExampleApplication.java).

<!-- 1 -->
