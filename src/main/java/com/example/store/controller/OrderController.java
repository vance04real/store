package com.example.store.controller;

import com.example.store.api.OrdersApi;
import com.example.store.model.OrderDTO;
import com.example.store.service.api.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @PostMapping
    @Override
    public ResponseEntity<OrderDTO> createOrder(OrderDTO orderDTO) {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);

        URI location = URI.create("/orders/" + createdOrder.getId());
        return ResponseEntity.created(location).body(createdOrder);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<OrderDTO>> getOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}
