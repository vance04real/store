package com.example.store.service.api;

import com.example.store.model.OrderDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * Retrieves all orders.
     *
     * @return a list of all orders as DTOs
     */
    Page<OrderDTO> getAllOrders(Pageable pageable);

    /**
     * Creates a new order.
     *
     * @param orderDTO the order entity to create
     * @return the created order as DTO
     */
    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO getOrderById(Long id);
}
