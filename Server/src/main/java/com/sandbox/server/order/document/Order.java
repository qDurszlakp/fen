package com.sandbox.server.order.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
public record Order(

        @Id
        String id,

        @Version
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

    public enum OrderStatus {NEW, PAID, SHIPPED, DELIVERED, CANCELLED}

    public record Address(String street, String city, String postalCode, String country) {
    }

    public record Item(String sku, String name, int quantity, BigDecimal price) {
    }
}
