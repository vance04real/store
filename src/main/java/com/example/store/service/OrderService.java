package com.example.store.service;

import com.example.store.dto.OrderDTO;
import com.example.store.entity.Order;

import java.util.List;

/**
 * Service interface for managing order operations.
 */
public interface OrderService {
    
    /**
     * Retrieves all orders.
     *
     * @return a list of all orders as DTOs
     */
    List<OrderDTO> getAllOrders();
    
    /**
     * Creates a new order.
     *
     * @param order the order entity to create
     * @return the created order as DTO
     */
    OrderDTO createOrder(Order order);
}