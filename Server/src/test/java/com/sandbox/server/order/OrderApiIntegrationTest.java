package com.sandbox.server.order;

import com.jayway.jsonpath.JsonPath;
import com.sandbox.MongoInfra;
import com.sandbox.BasicInfrastructure;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ImportTestcontainers(MongoInfra.class)
public class OrderApiIntegrationTest extends BasicInfrastructure {

    @Autowired
    private MockMvc mockMvc;

    private static final String NEW_ORDER = """
            {
              "customer": "anna",
              "status": "PAID",
              "currency": "PLN",
              "channel": "WEB",
              "note": "leave at the door",
              "address": { "street": "Dluga 1", "city": "Gdansk", "postalCode": "80-001", "country": "PL" },
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
                .content(NEW_ORDER));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.customer").value("anna"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.currency").value("PLN"))
                .andExpect(jsonPath("$.address.city").value("Gdansk"))
                .andExpect(jsonPath("$.address.country").value("PL"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.total").value(380.00))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void shouldCombineEveryFilterWithAnd() {
        // given
        createOrder();

        // when - all parameters are bound onto OrderFilter and ANDed together
        ResultActions result = mockMvc.perform(get("/orders")
                .param("customer", "anna")
                .param("status", "PAID")
                .param("currency", "PLN")
                .param("channel", "WEB")
                .param("city", "Gdansk")
                .param("country", "PL")
                .param("sku", "B-2")
                .param("minTotal", "300")
                .param("maxTotal", "400")
                .with(httpBasic("admin", "admin")));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customer").value("anna"));
    }

    @Test
    @SneakyThrows
    void shouldReturn404WhenUpdatingUnknownOrder() {
        // when
        ResultActions result = updateOrder("6a75bc568cfb9c7c32c14746", orderWith("0", "Sopot"));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("no order")));
    }

    @Test
    @SneakyThrows
    void shouldReturnNothingWhenOneFilterMisses() {
        // given
        createOrder();

        // when - the total range excludes it, everything else matches
        ResultActions result = mockMvc.perform(get("/orders")
                .param("city", "Gdansk")
                .param("minTotal", "1000")
                .with(httpBasic("admin", "admin")));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    void shouldFindOrderByFieldInsideEmbeddedDocument() {
        // given
        createOrder();

        // when
        ResultActions result = mockMvc.perform(get("/orders")
                .param("city", "Gdansk")
                .with(httpBasic("admin", "admin")));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].address.city").value("Gdansk"));
    }

    @Test
    @SneakyThrows
    void shouldBumpVersionOnEveryUpdate() {
        // given
        String created = createOrder();
        String id = read(created, "$.id");

        // when
        String updated = updateOrder(id, orderWith(read(created, "$.version"), "Sopot"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        assertThat(read(created, "$.version")).isEqualTo("0");
        assertThat(read(updated, "$.version")).isEqualTo("1");
        assertThat(read(updated, "$.address.city")).isEqualTo("Sopot");
    }

    @Test
    @SneakyThrows
    void shouldRejectSecondUpdateBuiltOnAStaleRead() {
        // given
        String created = createOrder();
        String id = read(created, "$.id");
        String versionBothRead = read(created, "$.version");

        // when
        updateOrder(id, orderWith(versionBothRead, "Sopot")).andExpect(status().isOk());

        // then
        updateOrder(id, orderWith(versionBothRead, "Gdynia"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Conflict")));

        // and then
        assertThat(cityOf(id)).isEqualTo("Sopot");
    }

    private String createOrder() throws Exception {
        return mockMvc.perform(post("/orders")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_ORDER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions updateOrder(String id, String payload) throws Exception {
        return mockMvc.perform(put("/orders/" + id)
                .with(httpBasic("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private String cityOf(String id) throws Exception {
        String all = mockMvc.perform(get("/orders").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> cities = JsonPath.read(all, "$[?(@.id == '" + id + "')].address.city");
        return cities.getFirst();
    }

    private static String read(String payload, String path) {
        Object value = JsonPath.read(payload, path);
        return String.valueOf(value);
    }

    private static String orderWith(String version, String city) {
        return """
                {
                  "version": %s,
                  "customer": "anna",
                  "status": "PAID",
                  "currency": "PLN",
                  "channel": "WEB",
                  "address": { "street": "Dluga 1", "city": "%s", "postalCode": "80-001", "country": "PL" },
                  "items": [ { "sku": "A-1", "name": "Keyboard", "quantity": 2, "price": 150.00 } ]
                }
                """.formatted(version, city);
    }
}
