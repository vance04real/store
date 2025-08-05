package com.example.store.dto;

import lombok.Data;

@Data
public class OrderCustomerDTO {

    private Long id;

    private String title;

    private String firstName;

    private String lastName;

    private String suffix;
}
