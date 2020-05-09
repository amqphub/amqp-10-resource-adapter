#!/bin/bash -ex

curl -X POST -d '{"text": "hello"}' -H 'Content-Type: application/json' http://localhost:8080/wildfly-example/api/send-request
curl -X POST http://localhost:8080/wildfly-example/api/receive-response
