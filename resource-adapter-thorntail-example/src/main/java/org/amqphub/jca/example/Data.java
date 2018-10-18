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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Data {
    private final Queue<String> requestIds;
    private final Map<String, Response> responses;
    private final Map<String, WorkerUpdate> workers;

    public Data() {
        this.requestIds = new ConcurrentLinkedQueue<>();
        this.responses = new ConcurrentHashMap<>();
        this.workers = new ConcurrentHashMap<>();
    }

    public Queue<String> getRequestIds() {
        return requestIds;
    }

    public Map<String, Response> getResponses() {
        return responses;
    }

    public Map<String, WorkerUpdate> getWorkers() {
        return workers;
    }

    @Override
    public String toString() {
        return String.format("Data{requestIds=%s, responses=%s, workers=%s}",
                             requestIds, responses, workers);
    }
}
