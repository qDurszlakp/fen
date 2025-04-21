package com.sandbox;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Lub inny webEnvironment wg potrzeb
public abstract class AbstractKafkaIntegrationTest {

//    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));
//
//    @DynamicPropertySource
//    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
//        String bootstrapServers = kafka.getBootstrapServers();
//        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);
//    }

}