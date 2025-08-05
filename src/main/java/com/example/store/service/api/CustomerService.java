package com.example.store.service.api;

import com.example.store.model.CustomerDTO;

import java.util.List;
import java.util.Optional;

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
     * @param customerDTO the customer entity to create
     * @return the created customer as DTO
     */
    CustomerDTO createCustomer(CustomerDTO customerDTO);


    /**
     * Returns a new customer.
     *
     * @param id of the customer to be retrieved
     * @return a customer as a DTO
     */
    Optional<CustomerDTO> getCustomerById(Long id);

}