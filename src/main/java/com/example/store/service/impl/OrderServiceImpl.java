package com.example.store.service.impl;

import com.example.store.config.CacheConfig;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.exception.handler.ProductNotFoundException;
import com.example.store.i18n.MessageKeys;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

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
                .orElseThrow(() -> {
                    String msg = messageSource.getMessage(
                            MessageKeys.CUSTOMER.NOT_FOUND,
                            new Object[]{orderDTO.getCustomerId()},
                            LocaleContextHolder.getLocale());
                    return new CustomerNotFoundException(msg);
                });
        order.setCustomer(customer);

        List<Product> products = productRepository.findAllById(orderDTO.getProductIds());
        if (products.size() != orderDTO.getProductIds().size()) {
            List<Long> foundIds = products.stream().map(Product::getId).toList();
            List<Long> missingIds = orderDTO.getProductIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            String msg = messageSource.getMessage(
                    MessageKeys.PRODUCT.MULTIPLE_NOT_FOUND,
                    new Object[]{missingIds},
                    LocaleContextHolder.getLocale());
            throw new ProductNotFoundException(msg);
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
                .orElseThrow(() -> {
                    String msg = messageSource.getMessage(
                            MessageKeys.ORDER.NOT_FOUND,
                            new Object[]{id},
                            LocaleContextHolder.getLocale());
                    return new OrderNotFoundException(msg);
                });
    }

    private void validateOrderCreation(OrderDTO orderDTO) {

        if (orderDTO.getDescription() == null || orderDTO.getDescription().trim().isEmpty()) {
            String msg = messageSource.getMessage(
                    MessageKeys.ORDER.DESCRIPTION_REQUIRED,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        if (orderDTO.getCustomerId() == null) {
            String msg = messageSource.getMessage(
                    MessageKeys.ORDER.CUSTOMER_ID_REQUIRED,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        if (orderDTO.getProductIds() == null || orderDTO.getProductIds().isEmpty()) {
            String msg = messageSource.getMessage(
                    MessageKeys.ORDER.PRODUCTS_REQUIRED,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        long uniqueProductCount = orderDTO.getProductIds().stream().distinct().count();
        if (uniqueProductCount != orderDTO.getProductIds().size()) {
            String msg = messageSource.getMessage(
                    MessageKeys.ORDER.PRODUCTS_DUPLICATE,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        log.debug("Order validation passed - Customer: {}, Products: {}",
                orderDTO.getCustomerId(), orderDTO.getProductIds().size());
    }
}