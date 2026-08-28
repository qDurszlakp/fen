package com.sandbox.server.order.infrastructure.persistence;

import com.sandbox.server.order.domain.model.Order;
import com.sandbox.server.order.domain.model.OrderFilter;
import com.sandbox.server.order.domain.model.OrderId;
import com.sandbox.server.order.domain.repository.OrderRepository;
import com.sandbox.server.order.infrastructure.persistence.mapper.OrderPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(order)));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return mongoRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Page<Order> search(OrderFilter filter, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        add(criteria, filter.customer(), value -> Criteria.where("customer").is(value));
        add(criteria, filter.status(), value -> Criteria.where("status").is(value.name()));
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

        long total = mongoTemplate.count(query, OrderMongoDocument.class);
        List<Order> page = mongoTemplate.find(query.with(pageable), OrderMongoDocument.class).stream()
                .map(mapper::toDomain)
                .toList();

        return new PageImpl<>(page, pageable, total);
    }

    private static <T> void add(List<Criteria> target, T value, Function<T, Criteria> asCriteria) {
        if (value != null) {
            target.add(asCriteria.apply(value));
        }
    }
}
