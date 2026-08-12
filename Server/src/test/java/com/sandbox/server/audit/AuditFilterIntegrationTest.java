package com.sandbox.server.audit;

import com.sandbox.BasicInfrastructure;
import com.sandbox.FixedClock;
import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import com.sandbox.server.audit.service.AuditService;
import com.sandbox.server.security.AppUser;
import com.sandbox.server.security.AppUserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static com.sandbox.AuthTestSupport.bearerAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(FixedClock.class)
public class AuditFilterIntegrationTest extends BasicInfrastructure {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditJpaRepository auditRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void clean() {
        auditRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldRecordTheAuthenticatedUsersUuid() {
        // given
        AppUser admin = appUserRepository.findByUsername("admin").orElseThrow();

        // when
        mockMvc.perform(get("/rest/cookies").with(bearerAuth(mockMvc)))
                .andExpect(status().isOk());

        // then
        List<Audit> rows = auditRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getUrl()).isEqualTo("/rest/cookies");
        assertThat(rows.getFirst().getUserUuid()).isEqualTo(admin.getId());
        assertThat(rows.getFirst().getActionTime()).isEqualTo(FixedClock.NOW);
        assertThat(rows.getFirst().getStatusCode()).isEqualTo(200);
    }

    @Test
    @SneakyThrows
    void shouldRecordACallRejectedForMissingCredentials() {
        // when - no Authorization header at all; never reaches the audit
        // filter, the entry point records it instead
        mockMvc.perform(get("/rest/cookies")).andExpect(status().isUnauthorized());

        // then
        List<Audit> rows = auditRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getUrl()).isEqualTo("/rest/cookies");
        assertThat(rows.getFirst().getUserUuid()).isEqualTo(AuditService.ANONYMOUS);
        assertThat(rows.getFirst().getStatusCode()).isEqualTo(401);
    }

    @Test
    @SneakyThrows
    void shouldRecordACallRejectedForAnInvalidToken() {
        // when - malformed token, so JwtDecoder never gets far enough to
        // read a "uuid" claim; same 401 path as a missing header
        mockMvc.perform(get("/rest/cookies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized());

        // then
        List<Audit> rows = auditRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getUserUuid()).isEqualTo(AuditService.ANONYMOUS);
    }

    @Test
    @SneakyThrows
    void shouldListAuditRowsFilteredByUserAndTimeRange() {
        // given - two audited calls, stamped with the pinned clock
        Instant from = FixedClock.NOW.minusSeconds(60);
        mockMvc.perform(get("/rest/cookies").with(bearerAuth(mockMvc))).andExpect(status().isOk());
        mockMvc.perform(get("/rest/risk").with(bearerAuth(mockMvc)));

        AppUser admin = appUserRepository.findByUsername("admin").orElseThrow();

        // when - filtered by user and a window that covers both
        mockMvc.perform(get("/audits")
                        .param("userUuid", admin.getId().toString())
                        .param("from", from.toString())
                        .param("to", FixedClock.NOW.plusSeconds(60).toString())
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].userUuid").value(admin.getId().toString()))
                .andExpect(jsonPath("$.content[0].actionTime").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void shouldPageTheListing() {
        // given - three audited calls
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/rest/cookies").with(bearerAuth(mockMvc))).andExpect(status().isOk());
        }

        // when - one row per page
        mockMvc.perform(get("/audits")
                        .param("size", "1")
                        .param("page", "0")
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    @SneakyThrows
    void shouldReturnNothingForATimeRangeInThePast() {
        // given
        mockMvc.perform(get("/rest/cookies").with(bearerAuth(mockMvc))).andExpect(status().isOk());

        // when
        mockMvc.perform(get("/audits")
                        .param("to", FixedClock.NOW.minusSeconds(3600).toString())
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @SneakyThrows
    void shouldForbidARegularUserFromListingAudits() {
        // when - valid token, but ROLE_USER, not ROLE_ADMIN
        mockMvc.perform(get("/audits").with(bearerAuth(mockMvc, "user", "user")))
                // then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @SneakyThrows
    void shouldNotRecordAnythingForAPermitAllEndpoint() {
        // when - reaches the filter, but with no authentication in the context
        mockMvc.perform(get("/actuator")).andExpect(status().isOk());

        // then
        assertThat(auditRepository.findAll()).isEmpty();
    }
}
