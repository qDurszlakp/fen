package com.sandbox.server.audit;

import com.sandbox.BasicInfrastructure;
import com.sandbox.server.audit.entity.Audit;
import com.sandbox.server.audit.repository.AuditJpaRepository;
import com.sandbox.server.security.AppUser;
import com.sandbox.server.security.AppUserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        Instant before = Instant.now();

        // when
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        // then
        List<Audit> rows = auditRepository.findAll();
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getUrl()).isEqualTo("/rest/cookies");
        assertThat(rows.getFirst().getUserUuid()).isEqualTo(admin.getId());
        assertThat(rows.getFirst().getActionTime()).isAfterOrEqualTo(before);
    }

    @Test
    @SneakyThrows
    void shouldNotRecordAnythingForAnUnauthenticatedCall() {
        // when - 401, so the request never reaches the audit filter
        mockMvc.perform(get("/rest/cookies")).andExpect(status().isUnauthorized());

        // then
        assertThat(auditRepository.findAll()).isEmpty();
    }

    @Test
    @SneakyThrows
    void shouldListAuditRowsFilteredByUserAndTimeRange() {
        // given - two audited calls
        Instant from = Instant.now().minusSeconds(60);
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin"))).andExpect(status().isOk());
        mockMvc.perform(get("/rest/risk").with(httpBasic("admin", "admin")));

        AppUser admin = appUserRepository.findByUsername("admin").orElseThrow();

        // when - filtered by user and a window that covers both
        mockMvc.perform(get("/audits")
                        .param("userUuid", admin.getId().toString())
                        .param("from", from.toString())
                        .param("to", Instant.now().plusSeconds(60).toString())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].userUuid").value(admin.getId().toString()))
                .andExpect(jsonPath("$[0].actionTime").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void shouldReturnNothingForATimeRangeInThePast() {
        // given
        mockMvc.perform(get("/rest/cookies").with(httpBasic("admin", "admin"))).andExpect(status().isOk());

        // when
        mockMvc.perform(get("/audits")
                        .param("to", Instant.now().minusSeconds(3600).toString())
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
