package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> findByNameContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE %:searchTerm% OR " +
            "LOWER(c.lastName) LIKE %:searchTerm%")
    List<Customer> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
