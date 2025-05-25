package com.sandbox;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestContainerSingleton {

    private static final KafkaContainer container;

    static {
        container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));
        container.start();
    }

    public static String getBootstrapServers() {
        return container.getBootstrapServers();
    }

}
