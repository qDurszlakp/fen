package com.sandbox.server.controller;

import com.sandbox.AbstractDbIntegrationTest;
import com.sandbox.server.kafka.FooLogConsumer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RestApiIntegrationTest extends AbstractDbIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FooLogConsumer fooLogConsumerMock;

    @Test
    @SneakyThrows
    void shouldReturnCookies() {
        // when
        ResultActions result = mockMvc.perform(get("/app/cookies"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Cookie"))
                .andExpect(jsonPath("$[0].value").value(1))
                .andExpect(jsonPath("$[1].name").value("Cookie"))
                .andExpect(jsonPath("$[1].value").value(2))
                .andExpect(jsonPath("$[2].name").value("Cookie"))
                .andExpect(jsonPath("$[2].value").value(3))
                .andExpect(jsonPath("$[3].name").value("Cookie"))
                .andExpect(jsonPath("$[3].value").value(4))
                .andExpect(jsonPath("$[4].name").value("Cookie"))
                .andExpect(jsonPath("$[4].value").value(5));
    }

}
