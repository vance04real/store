package com.example.store.service.impl;

import com.example.store.config.CacheConfig;
import com.example.store.entity.Customer;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.i18n.MessageKeys;
import com.example.store.model.CustomerDTO;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import com.example.store.service.api.CustomerService;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final MessageSource messageSource;

    @Override
    @Cacheable(value = CacheConfig.CUSTOMERS_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.info("Fetching customers page {} with size {} (cache miss)", pageable.getPageNumber(), pageable.getPageSize());
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerPage.map(customerMapper::customerToCustomerDTO);
    }

    @Override
    @CacheEvict(value = {CacheConfig.CUSTOMERS_CACHE, CacheConfig.CUSTOMER_SEARCH_CACHE}, allEntries = true)
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {

        log.info("Creating new customer: {} {}", customerDTO.getFirstName(), customerDTO.getLastName());

        validateCustomerCreation(customerDTO);

        var customer = customerMapper.customerDTOToCustomer(customerDTO);

        var savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully - ID: {}, Name: {} {}",
                savedCustomer.getId(), savedCustomer.getFirstName(), savedCustomer.getLastName());

        return customerMapper.customerToCustomerDTO(savedCustomer);
    }

    @Override
    @Cacheable(value = CacheConfig.CUSTOMERS_CACHE, key = "#id")
    public CustomerDTO getCustomerById(Long id) {

        log.info("Fetching customer {} from database (cache miss)", id);
        return customerRepository.findById(id)
                .map(customerMapper::customerToCustomerDTO)
                .orElseThrow(() -> {
                    String msg = messageSource.getMessage(
                            MessageKeys.CUSTOMER.NOT_FOUND,
                            new Object[]{id},
                            LocaleContextHolder.getLocale());
                    return new CustomerNotFoundException(msg);
                });
    }

    @Override
    @Cacheable(value = CacheConfig.CUSTOMER_SEARCH_CACHE, key = "#searchTerm + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CustomerDTO> searchCustomers(String searchTerm, Pageable pageable) {
        log.info("Searching customers with term '{}' page {} (cache miss)", searchTerm, pageable.getPageNumber());

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            String msg = messageSource.getMessage(
                    MessageKeys.CUSTOMER.SEARCH_TERM_EMPTY,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        Page<Customer> customerPage = customerRepository.findByNameContaining(searchTerm.trim(), pageable);
        return customerPage.map(customerMapper::customerToCustomerDTO);
    }


    private void validateCustomerCreation(CustomerDTO customerDTO) {

        if (customerDTO.getFirstName() == null || customerDTO.getFirstName().trim().isEmpty()) {
            String msg = messageSource.getMessage(
                    MessageKeys.CUSTOMER.FIRST_NAME_REQUIRED,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        if (customerDTO.getLastName() == null || customerDTO.getLastName().trim().isEmpty()) {
            String msg = messageSource.getMessage(
                    MessageKeys.CUSTOMER.LAST_NAME_REQUIRED,
                    null,
                    LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(msg);
        }

        log.info("Customer validation passed - Name: {} {}",
                customerDTO.getFirstName(), customerDTO.getLastName());
    }
}