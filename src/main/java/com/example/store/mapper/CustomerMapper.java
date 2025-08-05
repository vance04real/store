package com.example.store.mapper;

import com.example.store.model.CustomerDTO;
import com.example.store.model.CustomerOrderDTO;
import com.example.store.entity.Customer;

import com.example.store.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "orders", source = "orders")
    CustomerDTO customerToCustomerDTO(Customer customer);

    List<CustomerDTO> customersToCustomerDTOs(List<Customer> customers);

    CustomerOrderDTO orderToCustomerOrderDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer customerDTOToCustomer(CustomerDTO customerDTO);
}
