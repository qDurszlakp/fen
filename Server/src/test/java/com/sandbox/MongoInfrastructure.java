package com.sandbox;

import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@DataMongoTest
@ImportTestcontainers(MongoInfra.class)
public abstract class MongoInfrastructure {
}
