package com.sandbox.server.order.web;

import com.sandbox.server.order.application.OrderApplicationService;
import com.sandbox.server.order.domain.model.OrderId;
import com.sandbox.server.order.web.dto.OrderDto;
import com.sandbox.server.order.web.dto.OrderFilterDto;
import com.sandbox.server.order.web.mapper.OrderWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderWebMapper mapper;

    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto request) {
        var order = orderApplicationService.placeOrder(request.customer(), request.status(), request.currency(),
                request.channel(), request.note(), request.address(), request.items());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> update(@PathVariable String id, @RequestBody OrderDto request) {
        var order = orderApplicationService.updateOrder(new OrderId(id), mapper.toChanges(request));
        return ResponseEntity.ok(mapper.toDto(order));
    }

    @GetMapping
    public ResponseEntity<PagedModel<OrderDto>> orders(
            OrderFilterDto filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        var orders = orderApplicationService.findOrders(mapper.toDomainFilter(filter), pageable);
        return ResponseEntity.ok(new PagedModel<>(orders.map(mapper::toDto)));
    }
}
