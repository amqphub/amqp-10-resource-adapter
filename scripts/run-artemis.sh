#!/bin/bash -ex

exec docker run --rm -p 5672:5672 -e AMQ_USER=example -e AMQ_PASSWORD=example quay.io/artemiscloud/activemq-artemis-broker
