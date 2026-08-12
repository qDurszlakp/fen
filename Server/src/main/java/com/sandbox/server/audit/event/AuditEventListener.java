package com.sandbox.server.audit.event;

import com.sandbox.server.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AuditEventListener {

    private final AuditService auditService;

    @EventListener
    void on(AuditEvent event) {
        if (auditService.shouldAudit(event.url())) {
            auditService.record(event.url(), event.userId(), event.statusCode());
        }
    }
}
