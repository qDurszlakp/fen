package com.sandbox;

import com.sandbox.util.kafka.KafkaTopicInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class DbWithKafka {

    @Container
    @SuppressWarnings("resource")
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.1")
            .withDatabaseName("testDb")
            .withUsername("testUser")
            .withPassword("testPass");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", KafkaTestContainerSingleton::getBootstrapServers);

        attemptTopicsCreation();
    }

    private static void attemptTopicsCreation() {
        try (AdminClient adminClient = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaTestContainerSingleton.getBootstrapServers())
        )) {
            adminClient.createTopics(Collections.singletonList(
                    new NewTopic(KafkaTopicInfo.FOO_LOGGING.getTopicName(), 1, (short) 1)
            )).all().get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Exception during topic creation : {}", e.getMessage());
        }
    }

}
