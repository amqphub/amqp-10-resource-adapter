# Used in scripts/run-wildfly.sh, invoked from the project root

FROM quay.io/wildfly/wildfly-centos7:23.0

COPY resource-adapter/target/resource-adapter-*.rar /opt/wildfly/standalone/deployments/resource-adapter.rar
COPY wildfly-example/target/wildfly-example-*.war /opt/wildfly/standalone/deployments/wildfly-example.war
COPY wildfly-example/standalone-custom.xml /opt/wildfly/standalone/configuration/

CMD ["/opt/wildfly/bin/standalone.sh", "-c", "standalone-custom.xml", "-b", "localhost", "-bmanagement", "localhost"]
