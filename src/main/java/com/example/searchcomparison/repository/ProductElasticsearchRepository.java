package com.example.searchcomparison.repository;

import com.example.searchcomparison.model.Product;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository("elasticsearchProductRepository")
public interface ProductElasticsearchRepository extends PagingAndSortingRepository<Product, Long> {
    // ❌ No delete methods included
    // ✅ This ensures no delete queries are auto-generated
}
