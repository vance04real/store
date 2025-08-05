package com.example.store.service.impl;

import com.example.store.model.CustomerDTO;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import com.example.store.service.api.CustomerService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return List.of();
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        return null;
    }

    @Override
    public Optional<CustomerDTO> getCustomerById(Long id) {
        return Optional.empty();
    }
}