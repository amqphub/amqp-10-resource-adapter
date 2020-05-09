#!/bin/bash -ex

docker build -t wildfly-example .

exec docker run -it --rm --net host wildfly-example
