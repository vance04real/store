package com.example.store.controller;

import com.example.store.api.OrdersApi;
import com.example.store.model.OrderDTO;
import com.example.store.model.PaginatedOrderResponse;
import com.example.store.service.api.OrderService;
import com.example.store.utils.PaginationUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderService orderService;
    private final PaginationUtils paginationUtils;

    @PostMapping
    @Override
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        log.info(
                "Creating order for customer {} with {} products: {}",
                orderDTO.getCustomerId(),
                orderDTO.getProductIds() != null ? orderDTO.getProductIds().size() : 0,
                orderDTO.getProductIds());
        OrderDTO createdOrder = orderService.createOrder(orderDTO);

        URI location = URI.create("/orders/" + createdOrder.getId());
        return ResponseEntity.created(location).body(createdOrder);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        log.debug("Getting order - id: {}", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Override
    public ResponseEntity<PaginatedOrderResponse> getOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        log.debug("Getting orders - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        var pageable = paginationUtils.createPageable(page, size, sort, direction);

        Page<OrderDTO> ordersPage = orderService.getAllOrders(pageable);

        var orderResponse = PaginationUtils.toPaginatedOrderResponse(ordersPage);

        return ResponseEntity.ok(orderResponse);
    }
}
