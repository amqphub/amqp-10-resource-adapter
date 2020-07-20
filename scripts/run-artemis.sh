#!/bin/bash -ex

exec docker run --rm -p 5672:5672 -e ARTEMIS_USERNAME=example -e ARTEMIS_PASSWORD=example vromero/activemq-artemis:2.11.0-alpine
