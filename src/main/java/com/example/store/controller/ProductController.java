package com.example.store.controller;

import com.example.store.api.ProductsApi;
import com.example.store.model.PaginatedProductResponse;
import com.example.store.model.ProductDTO;
import com.example.store.service.api.ProductService;
import com.example.store.utils.PaginationUtils;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;

@Slf4j
@RestController("/products")
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    private final ProductService productService;

    @Override
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody @Valid ProductDTO productDTO) {
        log.info("Creating new product with description: '{}'",
                productDTO.getDescription());

        ProductDTO createdProduct = productService.createProduct(productDTO);

        URI location = URI.create("/products/" + createdProduct.getId());

        return ResponseEntity.created(location).body(createdProduct);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Override
    public ResponseEntity<PaginatedProductResponse> getProducts(@RequestParam(defaultValue = "0") Integer page,
                                                                @RequestParam(defaultValue = "20") Integer size,
                                                                @RequestParam(defaultValue = "id") String sort,
                                                                @RequestParam(defaultValue = "ASC") String direction) {

        log.debug("Getting products - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        var pageable = PaginationUtils.createPageable(page, size, sort, direction);

        Page<ProductDTO> productsPage = productService.getAllProducts(pageable);

        var productResponse = PaginationUtils.toPaginatedProductResponse(productsPage);

        return ResponseEntity.ok(productResponse);
    }
}
