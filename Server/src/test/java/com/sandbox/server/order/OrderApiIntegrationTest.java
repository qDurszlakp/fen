package com.sandbox.server.order;

import com.jayway.jsonpath.JsonPath;
import com.sandbox.MongoInfra;
import com.sandbox.BasicInfrastructure;
import com.sandbox.FixedClock;
import com.sandbox.server.order.infrastructure.persistence.OrderMongoRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.sandbox.AuthTestSupport.bearerAuth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ImportTestcontainers(MongoInfra.class)
@Import(FixedClock.class)
public class OrderApiIntegrationTest extends BasicInfrastructure {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderMongoRepository orderRepository;

    @BeforeEach
    void clean() {
        orderRepository.deleteAll();
    }

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
                .with(bearerAuth(mockMvc))
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
                .andExpect(jsonPath("$.createdAt").value(FixedClock.NOW.toString()));
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
                .with(bearerAuth(mockMvc)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].customer").value("anna"));
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
    void shouldPageTheListing() {
        // given
        createOrder();
        createOrder();
        createOrder();

        // when - one order per page
        mockMvc.perform(get("/orders")
                        .param("size", "1")
                        .param("page", "1")
                        .with(bearerAuth(mockMvc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.page.number").value(1));
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
                .with(bearerAuth(mockMvc)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    @SneakyThrows
    void shouldFindOrderByFieldInsideEmbeddedDocument() {
        // given
        createOrder();

        // when
        ResultActions result = mockMvc.perform(get("/orders")
                .param("city", "Gdansk")
                .with(bearerAuth(mockMvc)));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].address.city").value("Gdansk"));
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
                        .with(bearerAuth(mockMvc))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_ORDER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions updateOrder(String id, String payload) throws Exception {
        return mockMvc.perform(put("/orders/" + id)
                .with(bearerAuth(mockMvc))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private String cityOf(String id) throws Exception {
        String all = mockMvc.perform(get("/orders").with(bearerAuth(mockMvc)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> cities = JsonPath.read(all, "$.content[?(@.id == '" + id + "')].address.city");
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
