package com.sandbox.server.mongo.service;

import com.sandbox.server.mongo.document.Order;
import com.sandbox.server.mongo.repository.OrderMongoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderMongoRepository repository;
    private final MongoTemplate mongoTemplate;

    public Order create(Order order) {
        BigDecimal total = order.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return repository.save(new Order(
                null,
                order.customer(),
                order.address(),
                order.items(),
                total,
                Instant.now()
        ));
    }

    public List<Order> find(String customer, String city) {
        Query query = new Query();

        if (customer != null) {
            query.addCriteria(Criteria.where("customer").is(customer));
        }
        if (city != null) {
            query.addCriteria(Criteria.where("address.city").is(city));
        }

        return mongoTemplate.find(query, Order.class);
    }
}
