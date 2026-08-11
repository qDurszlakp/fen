package com.sandbox.server.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ExnHandler {

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

    @ExceptionHandler(OptimisticLockingFailureException.class)
    private ResponseEntity<Map<String, Object>> conflict(OptimisticLockingFailureException e) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", "Conflict - the document was modified by someone else, re-read it and retry");

        log.warn("Optimistic locking conflict: {}", e.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    /**
     * Thrown when a {@code @Valid @RequestBody} fails validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Map<String, Object>> validationError(MethodArgumentNotValidException e) {

        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        e.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));

        log.warn("Request body validation failed: {}", errors);

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
