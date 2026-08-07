package com.sandbox;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

public interface PostgresInfra {

    @ServiceConnection
    PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15.1")
            .withDatabaseName("testDb")
            .withUsername("testUser")
            .withPassword("testPass");

}
