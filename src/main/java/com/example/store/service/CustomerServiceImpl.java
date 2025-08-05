package com.example.store.service;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the CustomerService interface.
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerMapper.customersToCustomerDTOs(customerRepository.findAll());
    }

    @Override
    public CustomerDTO createCustomer(Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }
}