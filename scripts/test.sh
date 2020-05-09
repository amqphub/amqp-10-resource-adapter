#!/bin/bash -ex

scripts/run-artemis.sh &>> /tmp/artemis.log &
artemis_pid=$!

scripts/run-wildfly.sh &>> /tmp/wildfly.log &
wildfly_pid=$!

trap "kill $wildfly_pid $artemis_pid" EXIT

for i in {0..30}; do
    sleep 1
    curl -sfo /dev/null http://localhost:9990/health && break
done

scripts/run-curl-commands.sh
