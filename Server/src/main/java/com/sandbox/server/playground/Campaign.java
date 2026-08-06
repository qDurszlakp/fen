package com.sandbox.server.playground;

import java.time.LocalDate;
import java.util.UUID;

public record Campaign(
        UUID id,
        String code,
        LocalDate validFrom,
        LocalDate validTo,
        int priority
) {

    public static void foo() {
        System.out.println("Foo");
    }
}
