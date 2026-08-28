package com.sandbox.server.order.web.dto;

import com.sandbox.server.order.domain.model.OrderStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderFilterDto(

        String customer,

        OrderStatus status,

        String currency,

        String channel,

        String city,

        String country,

        String sku,

        BigDecimal minTotal,

        BigDecimal maxTotal,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdTo
) {
}
