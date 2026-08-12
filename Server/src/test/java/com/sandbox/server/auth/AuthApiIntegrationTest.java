package com.sandbox.server.auth;

import com.jayway.jsonpath.JsonPath;
import com.sandbox.BasicInfrastructure;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthApiIntegrationTest extends BasicInfrastructure {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void shouldIssueATokenPairForValidCredentials() {
        // when
        ResultActions result = login("admin", "admin", "10.0.0.1");

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(300)); // PT5M
    }

    @Test
    @SneakyThrows
    void shouldRejectAWrongPassword() {
        // when
        ResultActions result = login("admin", "wrong-password", "10.0.0.2");

        // then - one generic message, not "wrong password" specifically
        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @SneakyThrows
    void shouldAcceptTheIssuedAccessTokenOnAProtectedEndpoint() {
        // given
        String accessToken = accessTokenFor("admin", "admin", "10.0.0.3");

        // when
        mockMvc.perform(get("/rest/cookies").header("Authorization", "Bearer " + accessToken))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldLetTheSeededRegularUserLogInAndReachTheSameEndpointsAsAdmin() {
        // given - the two roles are identical everywhere except /audits
        String accessToken = accessTokenFor("user", "user", "10.0.0.6");

        // when
        mockMvc.perform(get("/rest/cookies").header("Authorization", "Bearer " + accessToken))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldRotateTheRefreshTokenAndInvalidateThePrevious() {
        // given
        String refreshToken = refreshTokenFor("admin", "admin", "10.0.0.4");

        // when - first use succeeds and returns a new pair
        ResultActions firstRefresh = mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(refreshToken)));

        // then
        firstRefresh.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(org.hamcrest.Matchers.not(refreshToken)));

        // and - reusing the spent token is a replay, not a valid refresh
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void shouldRevokeARefreshTokenOnLogout() {
        // given
        String refreshToken = refreshTokenFor("admin", "admin", "10.0.0.5");

        // when
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        // then - the revoked token can no longer be exchanged
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void shouldRateLimitRepeatedFailedLogins() {
        // given - an IP used by no other test in this class
        String ip = "10.0.0.42";

        // when - five failed attempts exhaust the bucket (MAX_ATTEMPTS = 5)
        for (int i = 0; i < 5; i++) {
            login("admin", "wrong-password", ip).andExpect(status().isUnauthorized());
        }

        // then - the sixth is rejected before credentials are even checked
        login("admin", "wrong-password", ip)
                .andExpect(status().isTooManyRequests());
    }

    private ResultActions login(String username, String password, String forwardedFor) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .header("X-Forwarded-For", forwardedFor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "%s", "password": "%s"}
                        """.formatted(username, password)));
    }

    private String accessTokenFor(String username, String password, String forwardedFor) throws Exception {
        String body = login(username, password, forwardedFor)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private String refreshTokenFor(String username, String password, String forwardedFor) throws Exception {
        String body = login(username, password, forwardedFor)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.refreshToken");
    }
}
