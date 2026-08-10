package com.sandbox.server.order.service;

import com.sandbox.server.order.document.Order;
import com.sandbox.server.order.dto.OrderFilter;
import com.sandbox.server.order.repository.OrderMongoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderMongoRepository repository;
    private final MongoTemplate mongoTemplate;

    public Order create(Order order) {
        return repository.save(new Order(
                null,
                null,
                order.customer(),
                order.status() != null ? order.status() : Order.OrderStatus.NEW,
                order.currency(),
                order.channel(),
                order.note(),
                order.address(),
                order.items(),
                total(order),
                Instant.now()
        ));
    }

    public Order update(String id, Order order) {
        Order existing = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no order " + id));

        return repository.save(new Order(
                id,
                order.version(),
                order.customer(),
                order.status() != null ? order.status() : existing.status(),
                order.currency(),
                order.channel(),
                order.note(),
                order.address(),
                order.items(),
                total(order),
                existing.createdAt()
        ));
    }

    public List<Order> find(OrderFilter filter) {
        List<Criteria> criteria = new ArrayList<>();

        add(criteria, filter.customer(), value -> Criteria.where("customer").is(value));
        add(criteria, filter.status(), value -> Criteria.where("status").is(value));
        add(criteria, filter.currency(), value -> Criteria.where("currency").is(value));
        add(criteria, filter.channel(), value -> Criteria.where("channel").is(value));
        add(criteria, filter.city(), value -> Criteria.where("address.city").is(value));
        add(criteria, filter.country(), value -> Criteria.where("address.country").is(value));
        add(criteria, filter.sku(), value -> Criteria.where("items.sku").is(value));
        add(criteria, filter.minTotal(), value -> Criteria.where("total").gte(value));
        add(criteria, filter.maxTotal(), value -> Criteria.where("total").lte(value));
        add(criteria, filter.createdFrom(), value -> Criteria.where("createdAt").gte(value));
        add(criteria, filter.createdTo(), value -> Criteria.where("createdAt").lte(value));

        Query query = criteria.isEmpty()
                ? new Query()
                : new Query(new Criteria().andOperator(criteria));

        return mongoTemplate.find(query, Order.class);
    }

    private static <T> void add(List<Criteria> target, T value, Function<T, Criteria> asCriteria) {
        if (value != null) {
            target.add(asCriteria.apply(value));
        }
    }

    private BigDecimal total(Order order) {
        if (order.items() == null) {
            return BigDecimal.ZERO;
        }
        return order.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
