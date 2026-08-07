package com.sandbox.server.mongo.controller;

import com.sandbox.server.mongo.document.Order;
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

    @GetMapping
    public ResponseEntity<List<Order>> orders(@RequestParam(required = false) String customer,
                                              @RequestParam(required = false) String city) {
        return ResponseEntity.ok(orderService.find(customer, city));
    }
}
