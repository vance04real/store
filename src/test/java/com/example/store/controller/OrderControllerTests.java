package com.example.store.controller;

import com.example.store.model.OrderDTO;
import com.example.store.service.api.OrderService;
import com.example.store.utils.PaginationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private PaginationUtils paginationUtils;

    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        orderDTO =
                new OrderDTO().id(1L).description("Test Order").customerId(1L).productIds(Set.of(1L, 2L));
    }

    @Test
    void testCreateOrder() throws Exception {
        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(orderDTO);

        OrderDTO createRequest =
                new OrderDTO().description("Test Order").customerId(1L).productIds(Set.of(1L));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/orders/1"))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customerId").value(1));
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderById(anyLong())).thenReturn(orderDTO);

        mockMvc.perform(get("/orders/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"));
    }

    @Test
    void testGetOrders() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(orderDTO));
        Pageable pageable = PageRequest.of(0, 20);

        when(paginationUtils.createPageable(
                        any(Integer.class), any(Integer.class), any(String.class), any(String.class)))
                .thenReturn(pageable);
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].description").value("Test Order"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(1));
    }
}
