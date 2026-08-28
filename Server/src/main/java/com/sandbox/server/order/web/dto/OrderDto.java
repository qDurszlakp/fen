package com.sandbox.server.order.web.dto;

import com.sandbox.server.order.domain.model.Address;
import com.sandbox.server.order.domain.model.Item;
import com.sandbox.server.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        String id,
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
}
