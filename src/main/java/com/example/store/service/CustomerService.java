package com.example.store.service;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;

import java.util.List;

/**
 * Service interface for managing customer operations.
 */
public interface CustomerService {
    
    /**
     * Retrieves all customers.
     *
     * @return a list of all customers as DTOs
     */
    List<CustomerDTO> getAllCustomers();
    
    /**
     * Creates a new customer.
     *
     * @param customer the customer entity to create
     * @return the created customer as DTO
     */
    CustomerDTO createCustomer(Customer customer);
}