package com.sandbox.server.banking.domain.model;

/**
 * Identity of an {@link Account} aggregate. {@code null} means "not persisted yet".
 */
public record AccountId(Long value) {
}
