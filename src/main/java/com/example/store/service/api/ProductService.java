package com.example.store.service.api;

import com.example.store.model.ProductDTO;

import java.util.List;
import java.util.Optional;


public interface ProductService {
    
    /**
     * Retrieves all products.
     *
     * @return a list of all products as DTOs
     */
    List<ProductDTO> getAllProducts();
    
    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to retrieve
     * @return the product as DTO if found, empty otherwise
     */
    Optional<ProductDTO> getProductById(Long id);
    
    /**
     * Creates a new product.
     *
     * @param productDTO the product entity to create
     * @return the created product as DTO
     */
    ProductDTO createProduct(ProductDTO productDTO);
}