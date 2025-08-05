package com.example.store.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerDTO {
    private Long id;
    private String title;
    private String firstName;
    private String lastName;
    private String suffix;
    private List<CustomerOrderDTO> orders;
}
