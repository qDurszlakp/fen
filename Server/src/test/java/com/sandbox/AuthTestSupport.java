package com.sandbox;

import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Replaces {@code SecurityMockMvcRequestPostProcessors.httpBasic(...)} now
 * that the application speaks Bearer tokens: logs in for real through
 * {@code POST /auth/login} so tests exercise the actual sign-and-verify
 * round trip, rather than injecting a fake {@code Authentication} straight
 * into the security context the way {@code jwt()} from spring-security-test
 * would.
 * <p>
 * One login per (MockMvc instance, username) pair is cached - a test class's
 * methods share the same {@link MockMvc}, and BCrypt is deliberately slow.
 * Keyed by identity on the outer map since {@code MockMvc} has no useful
 * {@code equals()}; two test classes never share an instance, so different
 * contexts never collide even though the RSA signing key is the same file
 * for all of them.
 */
public final class AuthTestSupport {

    private static final Map<MockMvc, Map<String, String>> TOKEN_CACHE = new IdentityHashMap<>();

    private AuthTestSupport() {
    }

    public static RequestPostProcessor bearerAuth(MockMvc mockMvc) {
        return bearerAuth(mockMvc, "admin", "admin");
    }

    public static synchronized RequestPostProcessor bearerAuth(MockMvc mockMvc, String username, String password) {
        Map<String, String> tokensForContext = TOKEN_CACHE.computeIfAbsent(mockMvc, mvc -> new ConcurrentHashMap<>());
        String token = tokensForContext.computeIfAbsent(username, name -> login(mockMvc, name, password));

        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }

    @SneakyThrows
    private static String login(MockMvc mockMvc, String username, String password) {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(body, "$.accessToken");
    }
}
