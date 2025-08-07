package com.example.store.controller;

import com.example.store.api.CustomersApi;
import com.example.store.model.CustomerDTO;
import com.example.store.model.PaginatedCustomerResponse;
import com.example.store.service.api.CustomerService;
import com.example.store.utils.PaginationUtils;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController implements CustomersApi {

    private final CustomerService customerService;

    @Override
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody @Valid CustomerDTO customerDTO) {
        log.info("Creating new customer: {} {}", customerDTO.getFirstName(), customerDTO.getLastName());

        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);

        URI location = URI.create("/customers/" + createdCustomer.getId());

        return ResponseEntity.created(location).body(createdCustomer);

    }

    @Override
    @GetMapping
    public ResponseEntity<PaginatedCustomerResponse> getCustomers(@RequestParam(defaultValue = "0") Integer page,
                                                                  @RequestParam(defaultValue = "20") Integer size,
                                                                  @RequestParam(defaultValue = "id") String sort,
                                                                  @RequestParam(defaultValue = "ASC") String direction) {
        log.debug("Getting customers - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        var pageable = PaginationUtils.createPageable(page, size, sort, direction);

        var customersPage = customerService.getAllCustomers(pageable);

        var response = PaginationUtils.toPaginatedCustomerResponse(customersPage);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<PaginatedCustomerResponse> searchCustomers(@RequestParam() String searchTerm,
                                                                     @RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "20") Integer size,
                                                                     @RequestParam(defaultValue = "firstName") String sort,
                                                                     @RequestParam(defaultValue = "ASC") String direction) {
        log.debug("Searching customers with term '{}' - page: {}, size: {}", searchTerm, page, size);

        var pageable = PaginationUtils.createPageable(page, size, sort, direction);

        var customersPage = customerService.searchCustomers(searchTerm, pageable);

        PaginatedCustomerResponse response = PaginationUtils.toPaginatedCustomerResponse(customersPage);

        return ResponseEntity.ok(response);

    }

}
