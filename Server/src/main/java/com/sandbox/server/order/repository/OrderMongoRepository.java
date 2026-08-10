package com.sandbox.server.order.repository;

import com.sandbox.server.order.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface OrderMongoRepository extends MongoRepository<Order, String> {
}
