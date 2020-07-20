#!/bin/bash -ex

scripts/run-artemis.sh &> /tmp/artemis.log &
artemis_pid=$!

scripts/run-wildfly.sh &> /tmp/wildfly.log &
wildfly_pid=$!

trap "kill $wildfly_pid $artemis_pid" EXIT

ready=0

for i in {0..60}; do
    sleep 5
    curl -sfo /dev/null http://localhost:9990/health && ready=1 && break
done

if (( $ready == 0 )); then
    echo "Error! WildFly never became ready"
    echo "-- ARTEMIS LOG --"
    cat /tmp/artemis.log
    echo "-- WILDFLY LOG --"
    cat /tmp/wildfly.log
    exit 1
fi

# XXX For some reason, the request queue is created as a topic if we
# don't have this sleep
sleep 5

scripts/run-curl-commands.sh
