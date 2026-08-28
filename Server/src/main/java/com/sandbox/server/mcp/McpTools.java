package com.sandbox.server.mcp;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.dto.AuditQuery;
import com.sandbox.server.audit.service.AuditService;
import com.sandbox.server.banking.application.AccountApplicationService;
import com.sandbox.server.banking.application.CardApplicationService;
import com.sandbox.server.banking.application.CountryApplicationService;
import com.sandbox.server.banking.domain.model.CountryCode;
import com.sandbox.server.banking.domain.model.CountryName;
import com.sandbox.server.banking.web.dto.AccountDto;
import com.sandbox.server.banking.web.dto.CardDto;
import com.sandbox.server.banking.web.dto.CountryDto;
import com.sandbox.server.banking.web.mapper.AccountWebMapper;
import com.sandbox.server.banking.web.mapper.CardWebMapper;
import com.sandbox.server.banking.web.mapper.CountryWebMapper;
import com.sandbox.server.order.application.OrderApplicationService;
import com.sandbox.server.order.domain.model.OrderFilter;
import com.sandbox.server.order.domain.model.OrderStatus;
import com.sandbox.server.order.web.dto.OrderDto;
import com.sandbox.server.order.web.mapper.OrderWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class McpTools {

    private final AccountApplicationService accountApplicationService;
    private final CardApplicationService cardApplicationService;
    private final CountryApplicationService countryApplicationService;
    private final AccountWebMapper accountWebMapper;
    private final CardWebMapper cardWebMapper;
    private final CountryWebMapper countryWebMapper;
    private final OrderApplicationService orderApplicationService;
    private final OrderWebMapper orderWebMapper;
    private final AuditService auditService;

    private static final int DEFAULT_SIZE = 20;

    @Tool(description = "Returns a page of accounts")
    public List<AccountDto> accounts(
            @ToolParam(required = false, description = "Page number, 0-based, defaults to 0") Integer page,
            @ToolParam(required = false, description = "Rows per page, defaults to 20") Integer size) {

        return accountApplicationService.listAccounts(pageOf(page, size)).map(accountWebMapper::toDto).getContent();
    }

    @Tool(description = "Returns a page of cards")
    public List<CardDto> cards(
            @ToolParam(required = false, description = "Page number, 0-based, defaults to 0") Integer page,
            @ToolParam(required = false, description = "Rows per page, defaults to 20") Integer size) {

        return cardApplicationService.listCards(pageOf(page, size)).map(cardWebMapper::toDto).getContent();
    }

    @Tool(description = "Creates a new country and returns the saved record")
    public CountryDto createCountry(
            @ToolParam(description = "Full country name, e.g. Poland") String name,
            @ToolParam(description = "ISO country code, e.g. PL") String code) {

        var country = countryApplicationService.registerCountry(new CountryName(name), new CountryCode(code));
        return countryWebMapper.toDto(country);
    }

    @Tool(description = "Finds orders. Every filter is optional; omitted ones are ignored")
    public List<OrderDto> orders(
            @ToolParam(required = false, description = "Customer name, e.g. anna") String customer,
            @ToolParam(required = false, description = "Order status: NEW, PAID, SHIPPED, DELIVERED or CANCELLED") OrderStatus status,
            @ToolParam(required = false, description = "Delivery city, e.g. Gdansk") String city,
            @ToolParam(required = false, description = "Product code of an ordered item, e.g. A-1") String sku,
            @ToolParam(required = false, description = "Lowest order total") BigDecimal minTotal,
            @ToolParam(required = false, description = "Highest order total") BigDecimal maxTotal,
            @ToolParam(required = false, description = "Page number, 0-based, defaults to 0") Integer page,
            @ToolParam(required = false, description = "Rows per page, defaults to 20") Integer size) {

        OrderFilter filter = new OrderFilter(
                customer, status, null, null, city, null, sku, minTotal, maxTotal, null, null);

        return orderApplicationService.findOrders(filter, pageOf(page, size)).map(orderWebMapper::toDto).getContent();
    }

    @Tool(description = "Returns audit entries, newest first. Every filter is optional")
    public List<AuditDto> audits(
            @ToolParam(required = false, description = "Identifier of the user who made the calls") UUID userUuid,
            @ToolParam(required = false, description = "Exact request path, e.g. /accounts") String url,
            @ToolParam(required = false, description = "Start of the time range, ISO-8601 in UTC, e.g. 2026-08-01T00:00:00Z") Instant from,
            @ToolParam(required = false, description = "End of the time range, ISO-8601 in UTC") Instant to,
            @ToolParam(required = false, description = "Page number, 0-based, defaults to 0") Integer page,
            @ToolParam(required = false, description = "Rows per page, defaults to 20") Integer size) {

        Pageable newestFirst = pageOf(page, size, Sort.by(Sort.Direction.DESC, "actionTime"));

        return auditService.find(new AuditQuery(userUuid, url, from, to), newestFirst).getContent();
    }

    private static Pageable pageOf(Integer page, Integer size) {
        return pageOf(page, size, Sort.unsorted());
    }

    private static Pageable pageOf(Integer page, Integer size, Sort sort) {
        return PageRequest.of(page != null ? page : 0, size != null ? size : DEFAULT_SIZE, sort);
    }
}
