package com.sandbox.server.order.application;

import com.sandbox.server.order.domain.model.Address;
import com.sandbox.server.order.domain.model.Item;
import com.sandbox.server.order.domain.model.Order;
import com.sandbox.server.order.domain.model.OrderFilter;
import com.sandbox.server.order.domain.model.OrderId;
import com.sandbox.server.order.domain.model.OrderStatus;
import com.sandbox.server.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public Order placeOrder(String customer, OrderStatus status, String currency, String channel, String note,
                             Address address, List<Item> items) {

        return orderRepository.save(Order.place(customer, status, currency, channel, note, address, items, Instant.now(clock)));
    }

    public Order updateOrder(OrderId id, Order changes) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no order " + id.value()));

        return orderRepository.save(existing.updatedFrom(changes));
    }

    public Page<Order> findOrders(OrderFilter filter, Pageable pageable) {
        return orderRepository.search(filter, pageable);
    }
}
