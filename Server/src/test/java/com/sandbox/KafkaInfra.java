package com.sandbox;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.ConfluentKafkaContainer;

public interface KafkaInfra {

    @ServiceConnection
    ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.8.0");

}
