package com.sandbox.server.mongo.repository;

import com.sandbox.server.mongo.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface OrderMongoRepository extends MongoRepository<Order, String> {
}
