package com.example.store.controller;

import com.example.store.api.CustomersApi;
import com.example.store.model.CustomerDTO;
import com.example.store.service.api.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CustomerService customerService;

    @PostMapping
    @Override
    public ResponseEntity<CustomerDTO> createCustomer(CustomerDTO customerDTO) {
        return null;
    }

    @GetMapping
    @Override
    public ResponseEntity<List<CustomerDTO>> getCustomers() {
        List<CustomerDTO> customerDtos = customerService.getAllCustomers();
        return ResponseEntity.ok(customerDtos);
    }
}
