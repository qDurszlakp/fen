package com.sandbox.server.auth.entity;

import com.sandbox.server.security.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "REFRESH_TOKENS")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TOKEN_ID", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_USER_ID", nullable = false, updatable = false)
    private AppUser user;

    @Column(name = "TOKEN_HASH", nullable = false, unique = true, updatable = false)
    private String tokenHash;

    @Column(name = "EXPIRES_AT", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "REVOKED", nullable = false)
    private boolean revoked;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;
}
