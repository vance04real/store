package com.example.store.service.impl;

import com.example.store.config.CacheConfig;
import com.example.store.entity.Product;
import com.example.store.exeption.handler.ProductNotFoundException;
import com.example.store.model.ProductDTO;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import com.example.store.service.api.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching products page {} with size {} (cache miss)", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(productMapper::productToProductDTO);
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "#id")
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product {} from database (cache miss)", id);
        return productRepository.findById(id)
                .map(productMapper::productToProductDTO)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @CacheEvict(value = {CacheConfig.PRODUCTS_CACHE, CacheConfig.ORDERS_CACHE}, allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.debug("Creating new product (will clear products and orders caches)");

        validateProductCreation(productDTO);
        Product product = productMapper.productDTOToProduct(productDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully - ID: {}, cleared product and order caches", savedProduct.getId());
        return productMapper.productToProductDTO(savedProduct);
    }

    private void validateProductCreation(ProductDTO productDTO) {
        if (productDTO.getDescription() == null || productDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Product description is required");
        }
    }
}