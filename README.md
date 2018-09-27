# AMQP JCA resource adapter

## Maven coordinates

    <dependency>
      <groupId>org.amqphub.jca</groupId>
      <artifactId>amqp-resource-adapter</artifactId>
      <version>${current-version}</version>
      <type>rar</type>
    </dependency>

## Thorntail configuration

    swarm:
      deployment:
        org.amqphub.jca:amqp-resource-adapter.rar:
      resource-adapters:
        resource-adapters:
          default:
            archive: amqp-resource-adapter.rar
            transaction-support: NoTransaction
            connection-definitions:
              default:
                jndi-name: java:global/jms/default
                class-name: org.jboss.resource.adapter.jms.JmsManagedConnectionFactory
                config-properties:
                  ConnectionFactory:
                    value: factory1
                  UserName:
                    value: work-queue
                  Password:
                    value: work-queue
                  JndiParameters:
                    value: "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory\
    .factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672}"
      ejb3:
        default-resource-adapter-name: default
      ee:
        annotation-property-replacement: true

## Example annotations

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
            @ActivationConfigProperty(propertyName = "user", propertyValue = "work-queue"),
            @ActivationConfigProperty(propertyName = "password", propertyValue = "work-queue"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
            @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672};queue.queue1=work-queue/responses"),
        })
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public class Example implements MessageListener {
        @Inject
        @JMSConnectionFactory("java:global/jms/default")
        private JMSContext jmsContext;

        @Override
        public void onMessage(Message message) {
        }
    }
