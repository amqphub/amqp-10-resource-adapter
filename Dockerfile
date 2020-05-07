FROM jboss/wildfly

COPY resource-adapter/target/resource-adapter-*.rar /opt/jboss/wildfly/standalone/deployments/resource-adapter.rar
COPY wildfly-example/target/resource-adapter-wildfly-example-*.war /opt/jboss/wildfly/standalone/deployments/
COPY wildfly-example/standalone-custom.xml /opt/jboss/wildfly/standalone/configuration/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-custom.xml", "-b", "localhost", "-bmanagement", "localhost"]
