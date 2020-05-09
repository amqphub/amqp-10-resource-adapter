#!/bin/bash -ex

docker build -t wildfly-example -f scripts/wildfly.dockerfile .

exec docker run -it --rm --net host wildfly-example
