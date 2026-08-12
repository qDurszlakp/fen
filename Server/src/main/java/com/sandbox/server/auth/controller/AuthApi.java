package com.sandbox.server.auth.controller;

import com.sandbox.server.audit.event.AuditEvent;
import com.sandbox.server.auth.dto.LoginRequest;
import com.sandbox.server.auth.dto.RefreshRequest;
import com.sandbox.server.auth.dto.TokenPairResponse;
import com.sandbox.server.auth.jwt.JwtService;
import com.sandbox.server.auth.service.RefreshTokenService;
import com.sandbox.server.security.AppUser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        AppUser user = (AppUser) authentication.getPrincipal();
        eventPublisher.publishEvent(new AuditEvent("/auth/login", user.getId(), HttpStatus.OK.value()));

        return ResponseEntity.ok(tokenPair(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshTokenService.Rotated rotated = refreshTokenService.rotate(request.refreshToken());

        String accessToken = jwtService.generateAccessToken(rotated.user());
        return ResponseEntity.ok(TokenPairResponse.of(accessToken, rotated.refreshToken(), jwtService.accessTokenTtl().toSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private TokenPairResponse tokenPair(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return TokenPairResponse.of(accessToken, refreshToken, jwtService.accessTokenTtl().toSeconds());
    }
}
