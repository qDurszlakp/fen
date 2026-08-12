package com.sandbox.server.audit.filter;

import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private static final String PATTERN_SPLITTER = ",";

    private final AuditJpaRepository auditRepository;
    private final Clock clock;

    @Value("${audit.patterns}")
    private String auditPatterns;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        if (shouldAudit(request.getRequestURI())) {
            currentUserId().ifPresent(userId -> auditRepository.save(createAudit(request, response, userId)));
        }
    }

    private Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication instanceof JwtAuthenticationToken jwtAuth
                ? Optional.of(UUID.fromString(jwtAuth.getToken().getClaimAsString("uuid")))
                : Optional.empty();
    }

    private boolean shouldAudit(String requestUri) {
        List<String> patternList = Arrays.asList(auditPatterns.split(PATTERN_SPLITTER));
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return patternList.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    Audit createAudit(HttpServletRequest request, HttpServletResponse response, UUID userId) {
        Audit auditEntity = new Audit();
        auditEntity.setUrl(request.getRequestURI());
        auditEntity.setUserUuid(userId);
        auditEntity.setActionTime(Instant.now(clock));
        auditEntity.setStatusCode(response.getStatus());
        return auditEntity;
    }
}
