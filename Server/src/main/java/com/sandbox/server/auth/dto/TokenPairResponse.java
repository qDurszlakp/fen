package com.sandbox.server.auth.dto;

public record TokenPairResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresIn
) {
    public static TokenPairResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
        return new TokenPairResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
    }
}
