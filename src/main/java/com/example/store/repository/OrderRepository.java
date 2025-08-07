package com.example.store.repository;

import com.example.store.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer", "products"})
    @NonNull
    Page<Order> findAll(@NonNull Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "products"})
    @NonNull
    Optional<Order> findById(@NonNull Long id);

}
