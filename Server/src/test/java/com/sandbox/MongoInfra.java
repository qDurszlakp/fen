package com.sandbox;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mongodb.MongoDBContainer;

public interface MongoInfra {

    @ServiceConnection
    MongoDBContainer mongo = new MongoDBContainer("mongo:8.2");

}
