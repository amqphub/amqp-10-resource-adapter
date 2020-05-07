#!/bin/bash -ex

podman build -t resource-adapter-wildfly-example .

exec podman run -p 8080:8080 -p 9990:9990 -it resource-adapter-wildfly-example
