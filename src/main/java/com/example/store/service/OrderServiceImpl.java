package com.example.store.service;

import com.example.store.dto.OrderDTO;
import com.example.store.entity.Order;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll());
    }

    @Override
    public OrderDTO createOrder(Order order) {
        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }
}