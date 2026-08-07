package com.sandbox.server.mongo;

import com.sandbox.DbWithKafka;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
public class OrderApiIntegrationTest extends DbWithKafka {

    @Container
    @SuppressWarnings("resource")
    // 8.0 refuses to start on kernel 6.19+ (SERVER-121912), 8.2 is fine
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.2");

    @DynamicPropertySource
    static void registerMongoProperties(DynamicPropertyRegistry registry) {
        // Spring Boot 4 renamed the prefix: spring.data.mongodb -> spring.mongodb
        registry.add("spring.mongodb.uri", mongo::getConnectionString);
        registry.add("spring.mongodb.database", () -> "sandbox");
    }

    @Autowired
    private MockMvc mockMvc;

    private static final String ORDER_JSON = """
            {
              "customer": "anna",
              "address": { "street": "Dluga 1", "city": "Gdansk", "postalCode": "80-001" },
              "items": [
                { "sku": "A-1", "name": "Keyboard", "quantity": 2, "price": 150.00 },
                { "sku": "B-2", "name": "Mouse",    "quantity": 1, "price": 80.00 }
              ]
            }
            """;

    @Test
    @SneakyThrows
    void shouldCreateOrderWithEmbeddedItemsAndComputeTotal() {
        // when
        ResultActions result = mockMvc.perform(post("/orders")
                .with(httpBasic("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ORDER_JSON));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())          // ObjectId assigned by Mongo
                .andExpect(jsonPath("$.customer").value("anna"))
                .andExpect(jsonPath("$.address.city").value("Gdansk"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.total").value(380.00))      // 2*150 + 1*80
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void shouldFindOrderByFieldInsideEmbeddedDocument() {
        // given
        mockMvc.perform(post("/orders")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDER_JSON))
                .andExpect(status().isCreated());

        // when
        ResultActions result = mockMvc.perform(get("/orders")
                .param("city", "Gdansk")
                .with(httpBasic("admin", "admin")));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].address.city").value("Gdansk"));
    }
}
