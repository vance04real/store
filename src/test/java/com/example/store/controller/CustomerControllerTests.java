package com.example.store.controller;

import com.example.store.model.CustomerDTO;
import com.example.store.service.api.CustomerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private PaginationUtils paginationUtils;

    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO()
                .id(1L)
                .title("Mr.")
                .firstName("John")
                .lastName("Doe")
                .suffix("Jr.");
    }

    @Test
    void testCreateCustomer() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(customerDTO);

        CustomerDTO createRequest =
                new CustomerDTO().title("Mr.").firstName("John").lastName("Doe").suffix("Jr.");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/customers/1"))
                .andExpect(jsonPath("$.title").value("Mr."))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.suffix").value("Jr."));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        Page<CustomerDTO> page = new PageImpl<>(List.of(customerDTO));
        Pageable pageable = PageRequest.of(0, 20);

        when(paginationUtils.createPageable(
                        any(Integer.class), any(Integer.class), any(String.class), any(String.class)))
                .thenReturn(pageable);
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Mr."))
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.data[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.data[0].suffix").value("Jr."))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(1));
    }
}
