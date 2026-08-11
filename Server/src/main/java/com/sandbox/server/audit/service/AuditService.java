package com.sandbox.server.audit.service;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.dto.AuditQuery;
import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.mapper.AuditMapper;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class AuditService {

    private static final String ACTION_TIME = "actionTime";
    public static final UUID ANONYMOUS = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final AuditJpaRepository auditRepository;
    private final AuditMapper auditMapper;
    private final Clock clock;

    @Transactional
    public void recordRejected(String url) {
        Audit audit = new Audit();
        audit.setUrl(url);
        audit.setUserUuid(ANONYMOUS);
        audit.setActionTime(Instant.now(clock));

        auditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public Page<AuditDto> find(AuditQuery query, Pageable pageable) {

        List<Specification<Audit>> specifications = new ArrayList<>();

        add(specifications, query.userUuid(),
                value -> (root, criteria, builder) -> builder.equal(root.get("userUuid"), value));
        add(specifications, query.url(),
                value -> (root, criteria, builder) -> builder.equal(root.get("url"), value));
        add(specifications, query.from(),
                value -> (root, criteria, builder) -> builder.greaterThanOrEqualTo(root.get(ACTION_TIME), value));
        add(specifications, query.to(),
                value -> (root, criteria, builder) -> builder.lessThanOrEqualTo(root.get(ACTION_TIME), value));

        return auditRepository.findAll(Specification.allOf(specifications), pageable)
                .map(auditMapper::auditToAuditDto);
    }

    private static <T> void add(List<Specification<Audit>> target, T value, Function<T, Specification<Audit>> asSpecification) {
        if (value != null) {
            target.add(asSpecification.apply(value));
        }
    }
}
