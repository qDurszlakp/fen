package com.sandbox.server.mongo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
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

        Address address,

        List<Item> items,

        BigDecimal total,

        Instant createdAt
) {

    public record Address(String street, String city, String postalCode) {
    }

    public record Item(String sku, String name, int quantity, BigDecimal price) {
    }
}
