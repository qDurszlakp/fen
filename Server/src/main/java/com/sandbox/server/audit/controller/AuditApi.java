package com.sandbox.server.audit.controller;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.dto.AuditQuery;
import com.sandbox.server.audit.service.AuditService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/audits")
public class AuditApi {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditDto>> audits(
            AuditQuery query,
            @PageableDefault(size = 20, sort = "actionTime", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(auditService.find(query, pageable));
    }
}
