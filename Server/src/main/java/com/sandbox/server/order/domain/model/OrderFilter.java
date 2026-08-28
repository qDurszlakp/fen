package com.sandbox.server.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderFilter(
        String customer,
        OrderStatus status,
        String currency,
        String channel,
        String city,
        String country,
        String sku,
        BigDecimal minTotal,
        BigDecimal maxTotal,
        Instant createdFrom,
        Instant createdTo
) {
}
