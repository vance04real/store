package com.example.store.mapper;

import com.example.store.entity.Product;
import com.example.store.model.ProductDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(
            target = "orders",
            expression =
                    "java(product.getOrders() != null ? product.getOrders().stream().map(order -> order.getId()).toList() : java.util.Collections.emptyList())")
    ProductDTO productToProductDTO(Product product);

    List<ProductDTO> productsToProductDTOs(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Product productDTOToProduct(ProductDTO productDTO);
}
