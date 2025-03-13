package com.example.searchcomparison.repository;

import com.example.searchcomparison.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);
    
    Page<Product> findAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:query)")
    Page<Product> searchExactProducts(@Param("query") String query, Pageable pageable);
}