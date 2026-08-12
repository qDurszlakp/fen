package com.sandbox.server.auth.service;

import com.sandbox.server.auth.entity.RefreshToken;
import com.sandbox.server.auth.exception.InvalidRefreshTokenException;
import com.sandbox.server.auth.repository.RefreshTokenRepository;
import com.sandbox.server.security.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final Clock clock;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.jwt.refresh-token-ttl:P7D}")
    private Duration refreshTokenTtl;

    @Transactional
    public String issue(AppUser user) {
        String rawToken = randomToken();
        Instant now = Instant.now(clock);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(now.plus(refreshTokenTtl));
        entity.setCreatedAt(now);

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public Rotated rotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("unknown refresh token"));

        if (existing.isRevoked()) {
            log.warn("Refresh token for user {} was reused after rotation - possible token theft", existing.getUser().getId());
            throw new InvalidRefreshTokenException("refresh token already used");
        }

        if (existing.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidRefreshTokenException("refresh token expired");
        }

        existing.setRevoked(true);
        String nextToken = issue(existing.getUser());

        return new Rotated(existing.getUser(), nextToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> token.setRevoked(true));
    }

    private String randomToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is guaranteed to be available on every JDK", e);
        }
    }

    public record Rotated(AppUser user, String refreshToken) {
    }
}
