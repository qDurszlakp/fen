package com.sandbox.server.audit.event;

import java.util.UUID;

public record AuditEvent(String url, UUID userId, int statusCode) {
}
