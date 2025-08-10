package com.example.store.service.api;

import com.example.store.model.CustomerDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    /**
     * Retrieves all customers.
     *
     * @return a list of all customers as DTOs
     */
    Page<CustomerDTO> getAllCustomers(Pageable pageable);

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
    CustomerDTO getCustomerById(Long id);

    Page<CustomerDTO> searchCustomers(String searchTerm, Pageable pageable);
}
