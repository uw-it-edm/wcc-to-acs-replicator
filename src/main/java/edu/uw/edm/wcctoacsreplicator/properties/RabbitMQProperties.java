package edu.uw.edm.wcctoacsreplicator.properties;

/*
 * Copyright 2014 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties related to sending and receiving events to and from RabbitMQ.
 *
 * @author James Renfro
 */
@Component
@ConfigurationProperties(prefix = "uw.replicator.rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {

    private String addresses = "localhost:5672";
    private String virtualHost = "/";
    private String username = "guest";
    private String password = "guest";
    private boolean useSSL = false;

    private int concurrentConsumers = 1;
    private int maxConcurrentConsumers = 1;

    private String replicatorQueueName = "replicator.item";
    private String replicatorQueueNameDeadLetterExchange = "replicator.item.deadletter";
    private boolean autoDeleteQueue = false;

    private String keystoreLocation;
    private String keystorePassword;
}