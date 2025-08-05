package com.example.store.controller;

import com.example.store.api.ProductsApi;
import com.example.store.model.ProductDTO;
import com.example.store.service.api.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController("/products")
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    private final ProductService productService;


    @Override
    public ResponseEntity<ProductDTO> createProduct(@RequestBody @Valid ProductDTO productDTO) {

        ProductDTO createdProduct = productService.createProduct(productDTO);

        URI location = URI.create("/products/" + createdProduct.getId());

        return ResponseEntity.created(location).body(createdProduct);
    }

    @Override
    public ResponseEntity<List<ProductDTO>> getProducts() {
        return null;
    }
}
