package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Override
    @EntityGraph(attributePaths = {"orders"})
    @NonNull Page<Customer> findAll(@NonNull Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"orders"})
    @NonNull Optional<Customer> findById(@NonNull Long id);

    @Query("SELECT c FROM Customer c WHERE " + "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findByNameContaining(@Param("searchTerm") String searchTerm, @NonNull Pageable pageable);
}
