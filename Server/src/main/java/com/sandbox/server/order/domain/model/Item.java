package com.sandbox.server.order.domain.model;

import java.math.BigDecimal;

public record Item(String sku, String name, int quantity, BigDecimal price) {

    BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
