package com.example.store.service.impl;

import com.example.store.model.OrderDTO;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.OrderRepository;

import com.example.store.service.api.OrderService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        return List.of();
    }

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        return null;
    }
}