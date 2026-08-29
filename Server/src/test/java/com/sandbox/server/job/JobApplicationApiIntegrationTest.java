package com.sandbox.server.job;

import com.sandbox.BasicInfrastructure;
import com.sandbox.FixedClock;
import com.sandbox.MongoInfra;
import com.jayway.jsonpath.JsonPath;
import com.sandbox.server.job.adapter.out.persistence.JobApplicationMongoRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.sandbox.AuthTestSupport.bearerAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportTestcontainers(MongoInfra.class)
@Import(FixedClock.class)
public class JobApplicationApiIntegrationTest extends BasicInfrastructure {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobApplicationMongoRepository jobApplicationMongoRepository;

    @BeforeEach
    void clean() {
        jobApplicationMongoRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldCreateJobApplicationWithCv() {
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());

        mockMvc.perform(multipart("/job-applications")
                        .file(cv)
                        .param("companyName", "Acme Corp")
                        .param("rateType", "HOURLY")
                        .param("rateAmount", "150")
                        .param("paidLeave", "true")
                        .param("vacationDays", "20")
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.rateType").value("HOURLY"))
                .andExpect(jsonPath("$.rateAmount").value(150))
                .andExpect(jsonPath("$.paidLeave").value(true))
                .andExpect(jsonPath("$.vacationDays").value(20))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.sentAt").value(FixedClock.NOW.toString()))
                .andExpect(jsonPath("$.updatedAt").value(FixedClock.NOW.toString()));

        assertThat(jobApplicationMongoRepository.count()).isEqualTo(1);
    }

    @Test
    @SneakyThrows
    void shouldCreateMonthlyRateApplication() {
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());

        mockMvc.perform(multipart("/job-applications")
                        .file(cv)
                        .param("companyName", "Acme Corp")
                        .param("rateType", "MONTHLY")
                        .param("rateAmount", "12000")
                        .param("paidLeave", "false")
                        .param("vacationDays", "0")
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rateType").value("MONTHLY"))
                .andExpect(jsonPath("$.rateAmount").value(12000))
                .andExpect(jsonPath("$.paidLeave").value(false));
    }

    @Test
    @SneakyThrows
    void shouldAllowRequestWithoutCv() {
        mockMvc.perform(multipart("/job-applications")
                        .param("companyName", "Acme Corp")
                        .param("rateType", "HOURLY")
                        .param("rateAmount", "150")
                        .param("paidLeave", "true")
                        .param("vacationDays", "20")
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void shouldUpdateStatus() {
        String created = createApplication();
        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(patch("/job-applications/" + id + "/status")
                        .with(bearerAuth(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "REJECTED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenUpdatingStatusOfUnknownApplication() {
        mockMvc.perform(patch("/job-applications/6a75bc568cfb9c7c32c14746/status")
                        .with(bearerAuth(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "REJECTED"}
                                """))
                .andExpect(status().is4xxClientError());
    }

    private String createApplication() throws Exception {
        MockMultipartFile cv = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());

        ResultActions result = mockMvc.perform(multipart("/job-applications")
                .file(cv)
                .param("companyName", "Acme Corp")
                .param("rateType", "HOURLY")
                .param("rateAmount", "150")
                .param("paidLeave", "true")
                .param("vacationDays", "20")
                .with(bearerAuth(mockMvc)));

        return result.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    }
}
