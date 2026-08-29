package com.sandbox.server.common.exception;

import com.sandbox.server.audit.event.AuditEvent;
import com.sandbox.server.audit.service.AuditService;
import com.sandbox.server.auth.exception.InvalidRefreshTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExnHandler {

    private final ApplicationEventPublisher eventPublisher;

    @ExceptionHandler(BasicException.class)
    private ResponseEntity<Map<String, Object>> genericError(BasicException e) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Generic Error");

        log.error("Undefined error: {}", e.getMessage(), e);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoSuchElementException.class)
    private ResponseEntity<Map<String, Object>> notFound(NoSuchElementException e) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", e.getMessage());

        log.warn("Not found: {}", e.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    private ResponseEntity<Map<String, Object>> badCredentials(AuthenticationException e, HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Invalid credentials");

        log.warn("Login failed: {}", e.getMessage());
        eventPublisher.publishEvent(new AuditEvent(request.getRequestURI(), AuditService.ANONYMOUS, HttpStatus.UNAUTHORIZED.value()));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    private ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException e) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Access denied");

        log.warn("Access denied: {}", e.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    private ResponseEntity<Map<String, Object>> invalidRefreshToken(InvalidRefreshTokenException e, HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Invalid refresh token");

        log.warn("Refresh token rejected: {}", e.getMessage());
        eventPublisher.publishEvent(new AuditEvent(request.getRequestURI(), AuditService.ANONYMOUS, HttpStatus.UNAUTHORIZED.value()));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    private ResponseEntity<Map<String, Object>> conflict(OptimisticLockingFailureException e) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Conflict - the document was modified by someone else, re-read it and retry");

        log.warn("Optimistic locking conflict: {}", e.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, Object>> validationError(MethodArgumentNotValidException e, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        e.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));

        log.warn("Request body validation failed: {}", errors);
        eventPublisher.publishEvent(new AuditEvent(request.getRequestURI(), AuditService.ANONYMOUS, HttpStatus.BAD_REQUEST.value()));

        return new ResponseEntity<>(validationBody(errors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Thrown when constraints placed directly on controller method parameters
     * (e.g. {@code @PathVariable}, {@code @RequestParam}) fail validation.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    private ResponseEntity<Map<String, Object>> validationError(HandlerMethodValidationException e) {

        Map<String, String> errors = new LinkedHashMap<>();
        e.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors()
                    .forEach(error -> errors.put(name, error.getDefaultMessage()));
        });

        log.warn("Request parameter validation failed: {}", errors);

        return new ResponseEntity<>(validationBody(errors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Thrown when validation fails on a {@code @Validated} bean outside the web layer.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<Map<String, Object>> validationError(ConstraintViolationException e) {

        Map<String, String> errors = new LinkedHashMap<>();
        e.getConstraintViolations()
                .forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        log.warn("Constraint validation failed: {}", errors);

        return new ResponseEntity<>(validationBody(errors), HttpStatus.BAD_REQUEST);
    }

    private Map<String, Object> validationBody(Map<String, String> errors) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Validation Error");
        body.put("errors", errors);

        return body;
    }

}
