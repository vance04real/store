package com.example.store.mapper;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.model.OrderCustomerDTO;
import com.example.store.model.OrderDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "productIds", source = "products", qualifiedByName = "mapProductIds")
    @Mapping(target = "customer", source = "customer")
    OrderDTO orderToOrderDTO(Order order);

    List<OrderDTO> ordersToOrderDTOs(List<Order> orders);

    OrderCustomerDTO customerToOrderCustomerDTO(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "products", ignore = true)
    Order orderDTOToOrder(OrderDTO orderDTO);

    @Named("mapProductIds")
    default Set<Long> mapProductIds(List<Product> products) {
        if (products == null) {
            return new LinkedHashSet<>();
        }
        return products.stream().map(Product::getId).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
