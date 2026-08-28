package com.sandbox.server.order.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderMongoRepository extends MongoRepository<OrderMongoDocument, String> {
}
