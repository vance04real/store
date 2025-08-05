package com.example.store.mapper;

import com.example.store.model.OrderCustomerDTO;
import com.example.store.model.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customer", source = "customer")
    OrderDTO orderToOrderDTO(Order order);

    List<OrderDTO> ordersToOrderDTOs(List<Order> orders);

    OrderCustomerDTO customerToOrderCustomerDTO(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "products", ignore = true)
    Order orderDTOToOrder(OrderDTO orderDTO);
}
