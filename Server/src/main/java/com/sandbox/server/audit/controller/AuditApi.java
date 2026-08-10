package com.sandbox.server.audit.controller;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.dto.AuditQuery;
import com.sandbox.server.audit.service.AuditService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/audits")
public class AuditApi {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditDto>> audits(AuditQuery query) {
        return ResponseEntity.ok(auditService.find(query));
    }
}
