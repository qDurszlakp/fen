package com.sandbox.server.mongo.controller;

import com.sandbox.server.mongo.document.Order;
import com.sandbox.server.mongo.dto.OrderFilter;
import com.sandbox.server.mongo.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
public class OrderApi {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable String id, @RequestBody Order order) {
        return ResponseEntity.ok(orderService.update(id, order));
    }

    @GetMapping
    public ResponseEntity<List<Order>> orders(OrderFilter filter) {
        return ResponseEntity.ok(orderService.find(filter));
    }
}
