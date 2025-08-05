package com.example.store.service.api;

import com.example.store.model.OrderDTO;
import java.util.List;

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
     * @param orderDTO the order entity to create
     * @return the created order as DTO
     */
    OrderDTO createOrder(OrderDTO orderDTO);
}