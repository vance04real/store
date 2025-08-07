package com.example.store.service.impl;

import com.example.store.config.CacheConfig;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exeption.CustomerNotFoundException;
import com.example.store.exeption.OrderNotFoundException;
import com.example.store.exeption.handler.ProductNotFoundException;
import com.example.store.model.OrderDTO;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;

import com.example.store.repository.ProductRepository;
import com.example.store.service.api.OrderService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Cacheable(value = CacheConfig.ORDERS_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.debug("Fetching orders page {} with size {} (cache miss)", pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(orderMapper::orderToOrderDTO);
    }

    @Override
    @CacheEvict(value = {
            CacheConfig.ORDERS_CACHE,
            CacheConfig.CUSTOMERS_CACHE,
            CacheConfig.PRODUCTS_CACHE,
            CacheConfig.CUSTOMER_SEARCH_CACHE
    }, allEntries = true)
    public OrderDTO createOrder(OrderDTO orderDTO) {

        log.debug("Creating new order (will clear all related caches)");

        validateOrderCreation(orderDTO);

        Order order = orderMapper.orderDTOToOrder(orderDTO);

        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(orderDTO.getCustomerId()));
        order.setCustomer(customer);

        List<Product> products = productRepository.findAllById(orderDTO.getProductIds());
        if (products.size() != orderDTO.getProductIds().size()) {
            List<Long> foundIds = products.stream().map(Product::getId).toList();
            List<Long> missingIds = orderDTO.getProductIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ProductNotFoundException("Products not found with IDs: " + missingIds);
        }
        order.setProducts(products);
        log.debug("Added {} products to order", products.size());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully - ID: {}, Customer: {}, Products: {}",
                savedOrder.getId(), savedOrder.getCustomer().getId(), savedOrder.getProducts().size());

        return orderMapper.orderToOrderDTO(savedOrder);
    }

    @Override
    @Cacheable(value = CacheConfig.ORDERS_CACHE, key = "#id")
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);

        return orderRepository.findById(id)
                .map(orderMapper::orderToOrderDTO)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void validateOrderCreation(OrderDTO orderDTO) {

        if (orderDTO.getDescription() == null || orderDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Order description is required");
        }

        if (orderDTO.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        if (orderDTO.getProductIds() == null || orderDTO.getProductIds().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product");
        }

        long uniqueProductCount = orderDTO.getProductIds().stream().distinct().count();
        if (uniqueProductCount != orderDTO.getProductIds().size()) {
            throw new IllegalArgumentException("Duplicate product IDs are not allowed");
        }

        log.debug("Order validation passed - Customer: {}, Products: {}",
                orderDTO.getCustomerId(), orderDTO.getProductIds().size());
    }
}