package com.example.store.service.api;

import com.example.store.model.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    /**
     * Retrieves all products.
     *
     * @return a list of all products as DTOs
     */
    Page<ProductDTO> getAllProducts(Pageable pageable);

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to retrieve
     * @return the product as DTO if found, empty otherwise
     */
    ProductDTO getProductById(Long id);

    /**
     * Creates a new product.
     *
     * @param productDTO the product entity to create
     * @return the created product as DTO
     */
    ProductDTO createProduct(ProductDTO productDTO);
}