package com.example.store.utils;

import com.example.store.model.CustomerDTO;
import com.example.store.model.OrderDTO;
import com.example.store.model.PaginatedCustomerResponse;
import com.example.store.model.PaginatedOrderResponse;
import com.example.store.model.PaginatedProductResponse;
import com.example.store.model.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PaginationUtils {

    public static void validatePaginationParams(Integer page, Integer size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }

    public static Pageable createPageable(Integer page, Integer size, String sort, String direction) {
        validatePaginationParams(page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(sortDirection, sort));
    }

    public static PaginatedCustomerResponse toPaginatedCustomerResponse(Page<CustomerDTO> page) {
        PaginatedCustomerResponse response = new PaginatedCustomerResponse();
        response.setData(page.getContent());
        response.setHasPages(page.getTotalPages() > 1);
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        return response;
    }

    public static PaginatedProductResponse toPaginatedProductResponse(Page<ProductDTO> page) {
        PaginatedProductResponse response = new PaginatedProductResponse();
        response.setData(page.getContent());
        response.setHasPages(page.getTotalPages() > 1);
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        return response;
    }

    public static PaginatedOrderResponse toPaginatedOrderResponse(Page<OrderDTO> page) {
        PaginatedOrderResponse response = new PaginatedOrderResponse();
        response.setData(page.getContent());
        response.setHasPages(page.getTotalPages() > 1);
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        return response;
    }
}
