package com.sandbox.server.audit.service;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.dto.AuditQuery;
import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.mapper.AuditMapper;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String ACTION_TIME = "actionTime";
    private static final String PATTERN_SPLITTER = ",";
    public static final UUID ANONYMOUS = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Clock clock;
    private final AuditMapper auditMapper;
    private final AuditJpaRepository auditRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${audit.patterns}")
    private String auditPatterns;

    @Transactional
    public void recordRejected(String url) {
        if (shouldAudit(url)) {
            record(url, ANONYMOUS, HttpStatus.UNAUTHORIZED.value());
        }
    }

    public boolean shouldAudit(String url) {
        List<String> patternList = Arrays.asList(auditPatterns.split(PATTERN_SPLITTER));
        return patternList.stream().anyMatch(pattern -> pathMatcher.match(pattern, url));
    }

    @Transactional
    public void record(String url, UUID userUuid, int statusCode) {
        Audit audit = new Audit();
        audit.setUrl(url);
        audit.setUserUuid(userUuid);
        audit.setActionTime(Instant.now(clock));
        audit.setStatusCode(statusCode);

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
