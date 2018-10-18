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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.jboss.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "factory1"),
        @ActivationConfigProperty(propertyName = "user", propertyValue = "work-queue"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "work-queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue1"),
        @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.apache.qpid.jms.jndi.JmsInitialContextFactory;connectionFactory.factory1=amqp://${env.MESSAGING_SERVICE_HOST:localhost}:${env.MESSAGING_SERVICE_PORT:5672};queue.queue1=work-queue/responses"),
    })
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ResponseListener implements MessageListener {
    private static final Logger log = Logger.getLogger(ResponseListener.class);

    @Inject
    private Frontend frontend;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        Response response;

        try {
            String requestId = textMessage.getJMSCorrelationID();
            String workerId = textMessage.getStringProperty("workerId");
            String text = textMessage.getText();

            response = new Response(requestId, workerId, text);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        frontend.getData().getResponses().put(response.getRequestId(), response);

        log.infof("%s: Received %s", frontend.id, response);
    }
}
