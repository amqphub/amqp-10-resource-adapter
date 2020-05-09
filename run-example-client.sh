#!/bin/bash -ex

curl -fX POST http://localhost:8080/wildfly-example/api/send-request -H "content-type: text/plain" -d "hellooo"
curl -fX POST http://localhost:8080/wildfly-example/api/receive-response
