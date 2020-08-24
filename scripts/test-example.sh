#!/bin/bash -ex

scripts/run-artemis.sh &> /tmp/artemis.log &
artemis_pid=$!

scripts/run-wildfly.sh &> /tmp/wildfly.log &
wildfly_pid=$!

trap "kill $wildfly_pid $artemis_pid" EXIT

function fail {
    echo "-- ERROR --"
    echo $1
    echo "-- ARTEMIS LOG --"
    cat /tmp/artemis.log
    echo "-- WILDFLY LOG --"
    cat /tmp/wildfly.log
    echo "-- ERROR --"
    echo $1
    echo "Test result: FAILED"
    exit 1
}

ready=0

for i in {0..60}; do
    sleep 10
    curl -sfo /dev/null http://localhost:9990/health && ready=1 && break
done

if (( $ready == 0 )); then
    fail "WildFly did not become ready"
fi

scripts/run-curl-commands.sh || fail "Curl command failed"

echo "Test result: PASSED"
