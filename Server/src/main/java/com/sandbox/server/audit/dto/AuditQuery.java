package com.sandbox.server.audit.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

public record AuditQuery(

        UUID userUuid,

        String url,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant to
) {
}
