package com.sandbox.server.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditDto(

        Long id,

        UUID userUuid,

        String url,

        Instant actionTime,

        Integer statusCode
) {
}
