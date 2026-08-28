package com.sandbox.server.order.domain.repository;

import com.sandbox.server.order.domain.model.Order;
import com.sandbox.server.order.domain.model.OrderFilter;
import com.sandbox.server.order.domain.model.OrderId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    Page<Order> search(OrderFilter filter, Pageable pageable);
}
