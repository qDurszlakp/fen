package com.sandbox.server.order.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
public record OrderMongoDocument(

        @Id
        String id,

        @Version
        Long version,

        String customer,

        String status,

        String currency,

        String channel,

        String note,

        AddressDocument address,

        List<ItemDocument> items,

        BigDecimal total,

        Instant createdAt
) {

    public record AddressDocument(String street, String city, String postalCode, String country) {
    }

    public record ItemDocument(String sku, String name, int quantity, BigDecimal price) {
    }
}
