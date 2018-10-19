# AMQP 1.0 resource adapter - Thorntail example

    mvn thorntail:run
    curl -v -X POST -d '{"text": "hello"}' -H 'Content-Type: application/json' http://localhost:8080/api/send-request
    -> <message-id>
    curl -v -X POST http://localhost:8080/api/receive-response
    -> <message-id>: HELLO
