# AMQP 1.0 resource adapter - Thorntail example

1. Start an AMQP 1.0 server on localhost and port 5672.  If your
   server does not create queues on demand, use the tools for your
   server to create queues called `example/requests` and
   `example/responses`.

2. Build and run the example code.

        $ mvn thorntail:run

3. In another shell, use `curl` to send text to the `send-request` endpoint.

        $ curl -X POST -d '{"text": "hello"}' -H 'Content-Type: application/json' http://localhost:8080/api/send-request
        ID:4a63adc0-547c-4881-bc3e-3c8eb7007648:2:1:1-1

4. Use `curl` again to get the response from the `receive-response` endpoint.

        $ curl -X POST http://localhost:8080/api/receive-response
        ID:4a63adc0-547c-4881-bc3e-3c8eb7007648:2:1:1-1: HELLO
