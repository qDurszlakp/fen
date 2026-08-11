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
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        // then
        List<Audit> rows = auditRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getUrl()).isEqualTo("/rest/cookies");
        assertThat(rows.getFirst().getUserUuid()).isEqualTo(admin.getId());
        assertThat(rows.getFirst().getActionTime()).isEqualTo(FixedClock.NOW);
    }

    @Test
    @SneakyThrows
    void shouldRecordACallRejectedForMissingCredentials() {
        // when - never reaches the audit filter, the entry point records it instead
        mockMvc.perform(get("/rest/cookies")).andExpect(status().isUnauthorized());

        // then
        List<Audit> rows = auditRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getUrl()).isEqualTo("/rest/cookies");
        assertThat(rows.getFirst().getUserUuid()).isEqualTo(AuditService.ANONYMOUS);
    }

    @Test
    @SneakyThrows
    void shouldRecordACallRejectedForWrongCredentials() {
        // when
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "wrong-password")))
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
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin"))).andExpect(status().isOk());
        mockMvc.perform(get("/rest/risk").with(httpBasic("admin", "admin")));

        AppUser admin = appUserRepository.findByUsername("admin").orElseThrow();

        // when - filtered by user and a window that covers both
        mockMvc.perform(get("/audits")
                        .param("userUuid", admin.getId().toString())
                        .param("from", from.toString())
                        .param("to", FixedClock.NOW.plusSeconds(60).toString())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].userUuid").value(admin.getId().toString()))
                .andExpect(jsonPath("$.content[0].actionTime").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void shouldPageTheListing() {
        // given - three audited calls
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin"))).andExpect(status().isOk());
        }

        // when - one row per page
        mockMvc.perform(get("/audits")
                        .param("size", "1")
                        .param("page", "0")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(4))
                .andExpect(jsonPath("$.page.totalPages").value(4))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    @SneakyThrows
    void shouldReturnNothingForATimeRangeInThePast() {
        // given
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin"))).andExpect(status().isOk());

        // when
        mockMvc.perform(get("/audits")
                        .param("to", FixedClock.NOW.minusSeconds(3600).toString())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.content.length()").value(0));
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
