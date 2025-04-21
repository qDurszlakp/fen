package com.sandbox;

import org.junit.ClassRule;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractDbIntegrationTest {

    @ClassRule
    @SuppressWarnings("resource")
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.1")
            .withDatabaseName("testDb")
            .withUsername("testUser")
            .withPassword("testPass");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

}
