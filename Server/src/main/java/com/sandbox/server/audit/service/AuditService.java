package com.sandbox.server.audit.service;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.dto.AuditQuery;
import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.mapper.AuditMapper;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class AuditService {

    private static final String ACTION_TIME = "actionTime";

    private final AuditJpaRepository auditRepository;
    private final AuditMapper auditMapper;

    @Transactional(readOnly = true)
    public List<AuditDto> find(AuditQuery query) {

        List<Specification<Audit>> specifications = new ArrayList<>();

        add(specifications, query.userUuid(),
                value -> (root, criteria, builder) -> builder.equal(root.get("userUuid"), value));
        add(specifications, query.url(),
                value -> (root, criteria, builder) -> builder.equal(root.get("url"), value));
        add(specifications, query.from(),
                value -> (root, criteria, builder) -> builder.greaterThanOrEqualTo(root.get(ACTION_TIME), value));
        add(specifications, query.to(),
                value -> (root, criteria, builder) -> builder.lessThanOrEqualTo(root.get(ACTION_TIME), value));

        List<Audit> rows = auditRepository.findAll(
                Specification.allOf(specifications),
                Sort.by(Sort.Direction.DESC, ACTION_TIME));

        return auditMapper.auditsToAuditDtos(rows);
    }

    private static <T> void add(List<Specification<Audit>> target, T value, Function<T, Specification<Audit>> asSpecification) {
        if (value != null) {
            target.add(asSpecification.apply(value));
        }
    }
}
