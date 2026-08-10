package com.sandbox.server.audit.filter;

import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private static final String PATTERN_SPLITTER = ",";
    private static final String SYSTEM_UUID = "3f5c2a63-50d9-4f21-b67a-f2cb8c8f7e75";

    private final AuditJpaRepository auditRepository;

    @Value("${audit.patterns}")
    private String auditPatterns;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (shouldAudit(request.getRequestURI())) {
            Audit auditEntity = createAudit(request);
            auditRepository.save(auditEntity);
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldAudit(String requestUri) {
        List<String> patternList = Arrays.asList(auditPatterns.split(PATTERN_SPLITTER));
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return patternList.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    Audit createAudit(HttpServletRequest request) {
        Audit auditEntity = new Audit();
        auditEntity.setUrl(request.getRequestURI());
        auditEntity.setUserUUID(UUID.fromString(SYSTEM_UUID));
        auditEntity.setActionTime(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")));
        return auditEntity;
    }
}
