package com.sandbox.server.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Order(
        OrderId id,
        Long version,
        String customer,
        OrderStatus status,
        String currency,
        String channel,
        String note,
        Address address,
        List<Item> items,
        BigDecimal total,
        Instant createdAt
) {

    public Order {
        if (customer == null || customer.isBlank()) {
            throw new IllegalArgumentException("Order customer must not be blank");
        }
        items = items == null ? List.of() : List.copyOf(items);
        total = total(items);
    }

    public static Order place(String customer, OrderStatus status, String currency, String channel, String note,
                               Address address, List<Item> items, Instant createdAt) {
        return new Order(
                null,
                null,
                customer,
                status != null ? status : OrderStatus.NEW,
                currency,
                channel,
                note,
                address,
                items,
                total(items),
                createdAt
        );
    }

    public Order updatedFrom(Order changes) {
        return new Order(
                id,
                changes.version(),
                changes.customer(),
                changes.status() != null ? changes.status() : status,
                changes.currency(),
                changes.channel(),
                changes.note(),
                changes.address(),
                changes.items(),
                total(changes.items()),
                createdAt
        );
    }

    private static BigDecimal total(List<Item> items) {
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(Item::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
