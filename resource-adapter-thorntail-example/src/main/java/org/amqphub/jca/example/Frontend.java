/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.amqphub.jca.example;

import java.util.Map;
import java.util.UUID;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Singleton
@ApplicationPath("/api")
@Path("/")
public class Frontend extends Application {
    private static final Logger log = Logger.getLogger(Frontend.class);
    static final String id = "frontend-wfswarm-" + UUID.randomUUID()
        .toString().substring(0, 4);
    private final Data data;

    @Inject
    @JMSConnectionFactory("java:global/jms/default")
    private JMSContext jmsContext;

    public Frontend() {
        this.data = new Data();
    }

    @POST
    @Path("send-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String sendRequest(Request request) {
        log.infof("%s: Sending %s", id, request);

        Queue requests = jmsContext.createQueue("work-queue/requests");
        Queue responses = jmsContext.createQueue("work-queue/responses");
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage();

        try {
            message.setJMSReplyTo(responses);
            message.setBooleanProperty("uppercase", request.isUppercase());
            message.setBooleanProperty("reverse", request.isReverse());
            message.setText(request.getText());

            producer.send(requests, message);

            getData().getRequestIds().add(message.getJMSMessageID());

            return message.getJMSMessageID();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("receive-response")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveResponse(@QueryParam("request") String requestId) {
        if (requestId == null) {
            throw new BadRequestException("A 'request' parameter is required");
        }
        
        Response response = getData().getResponses().get(requestId);

        if (response == null) {
            throw new NotFoundException();
        }

        return response;
    }

    @GET
    @Path("data")
    @Produces(MediaType.APPLICATION_JSON)
    public Data getData() {
        return data;
    }

    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void pruneStaleWorkers() {
        log.debugf("%s: Pruning stale workers", id);

        Map<String, WorkerUpdate> workers = getData().getWorkers();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, WorkerUpdate> entry : workers.entrySet()) {
            String workerId = entry.getKey();
            WorkerUpdate update = entry.getValue();

            if (now - update.getTimestamp() > 10 * 1000) {
                workers.remove(workerId);
                log.infof("%s: Pruned %s", id, workerId);
            }
        }
    }
}
