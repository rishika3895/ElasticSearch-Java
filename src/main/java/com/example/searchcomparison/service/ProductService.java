package com.example.searchcomparison.service;

import com.example.searchcomparison.model.Product;
import com.example.searchcomparison.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.json.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    private static final Logger log = Logger.getLogger(ProductService.class.getName());
    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String INDEX_NAME = "products";
    private boolean indexingComplete = false;
    
    @Autowired
    public ProductService(ProductRepository productRepository, ElasticsearchOperations elasticsearchOperations) {
        this.productRepository = productRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    public Map<String, Object> searchProducts(String query, int page, int size, boolean strict) {
        Map<String, Object> result = new HashMap<>();
        Pageable pageable = PageRequest.of(page, size);
        long startTime;
        long endTime;

        try {
            // Traditional SQL Search
            startTime = System.currentTimeMillis();
            Page<Product> traditionalResults = searchTraditional(query, pageable);
            endTime = System.currentTimeMillis();
            result.put("traditionalSearchTime", endTime - startTime);
            result.put("traditionalResults", traditionalResults.getContent());
            result.put("currentPage", page);
            result.put("totalPages", traditionalResults.getTotalPages());
            result.put("totalElements", traditionalResults.getTotalElements());

            // Elasticsearch Search
            startTime = System.currentTimeMillis();
            List<Product> elasticResults;
            
            if (strict) {
                elasticResults = searchElasticsearchExact(query);
            } else {
                elasticResults = searchElasticsearchSimilar(query);
            }
            
            endTime = System.currentTimeMillis();
            result.put("elasticSearchTime", endTime - startTime);
            result.put("elasticResults", elasticResults);

            if (elasticResults.isEmpty() && traditionalResults.isEmpty()) {
                result.put("message", "No matches found.");
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error during search operation", e);
            result.put("error", "An error occurred during the search operation");
        }

        return result;
    }

    private Page<Product> searchTraditional(String query, Pageable pageable) {
        return productRepository.searchExactProducts(query, pageable);
    }

    private List<Product> searchElasticsearchExact(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // 🔍 Optimized Exact Match Search with performance improvements
            NativeQuery exactMatchQuery = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t
                    .field("name.keyword")
                    .value(v -> v.stringValue(query))))
                // Remove source filtering that's causing errors
                // Enable request cache for faster repeat queries
                .withRequestCache(true)
                // Set preference to _local to prefer local shards
                .withPreference("_local")
                // Set timeout to prevent long-running queries
                //.withMaxTime("1s")
                .build();
            
            log.info("Executing optimized exact match query for: " + query);

            SearchHits<Product> exactMatchHits = elasticsearchOperations.search(
                exactMatchQuery, Product.class, IndexCoordinates.of(INDEX_NAME));

            List<Product> exactMatchResults = exactMatchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            log.info("Found " + exactMatchResults.size() + " exact matches for query: " + query);
            return exactMatchResults;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Failed to execute exact match search in Elasticsearch", e);
            return Collections.emptyList();
        }
    }

    private List<Product> searchElasticsearchSimilar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Optimized search query with better performance
            NativeQuery optimizedQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                    // Use term query for exact matches which is faster
                    .should(s -> s.term(t -> t
                        .field("name.keyword")
                        .value(v -> v.stringValue(query))
                        .boost(2.0f)))
                    // Use prefix query instead of wildcard for better performance
                    .should(s -> s.prefix(p -> p
                        .field("name")
                        .value(query.toLowerCase())
                        .boost(1.5f)))
                    // Use match query with minimum_should_match for partial word matches
                    .should(s -> s.match(m -> m
                        .field("name")
                        .query(query)
                        .minimumShouldMatch("2<70%")
                        .boost(1.0f)))
                    .minimumShouldMatch("1")))
                // Remove source filtering that's causing errors
                // Cache the query results
                .withRequestCache(true)
                .withTrackScores(true)
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .build();

            log.info("Executing optimized search for query: " + query);
            return executeSearch(optimizedQuery);
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error in similar Elasticsearch search", e);
            return Collections.emptyList();
        }
    }

    private List<Product> executeSearch(NativeQuery searchQuery) {
        try {
            SearchHits<Product> searchHits = elasticsearchOperations.search(
                searchQuery, Product.class, IndexCoordinates.of(INDEX_NAME));
            
            return searchHits.getSearchHits().stream()
                .map(hit -> {
                    Product product = hit.getContent();
                    if (product != null) {
                        try {
                            String esId = hit.getId();
                            if (esId.matches("\\d+")) { // 🔥 Only set ID if it's numeric
                                product.setId(Long.parseLong(esId));
                            } else {
                                log.warning("Non-numeric ID found in Elasticsearch: " + esId);
                                product.setId(null); // 🔥 Prevent crash
                            }
                        } catch (NumberFormatException e) {
                            log.warning("Invalid ID format in Elasticsearch: " + hit.getId());
                            product.setId(null); // 🔥 Prevent exception
                        }
                    }
                    return product;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.log(Level.WARNING, "Error executing Elasticsearch search: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @PostConstruct
    public void initializeElasticsearchIndex() {
        log.info("🚀 Starting Elasticsearch product indexing during application startup...");
        indexProductsInElasticsearch();
    }
    
    public void indexProductsInElasticsearch() {
        if (indexingComplete) {
            log.info("⏭️ Skipping indexing as it was already completed");
            return;
        }
        
        try {
            log.info("📊 Fetching all products from database for Elasticsearch indexing");
            List<Product> allProducts = productRepository.findAll();
            log.info("📈 Found " + allProducts.size() + " products to index in Elasticsearch");
            
            int batchSize = 50000;
            int totalBatches = (int) Math.ceil((double) allProducts.size() / batchSize);
            log.info("🔄 Will process indexing in " + totalBatches + " batches of " + batchSize + " products each");
    
            for (int i = 0; i < allProducts.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                int batchStart = i;
                int batchEnd = Math.min(i + batchSize, allProducts.size());
                log.info("⏳ Processing batch " + batchNumber + " of " + totalBatches + " (products " + batchStart + " to " + batchEnd + ")");
                
                List<Product> batch = allProducts.subList(batchStart, batchEnd);
    
                List<IndexQuery> indexQueries = batch.stream()
                    .map(product -> {
                        String elasticId = String.valueOf(product.getId()); // 🔥 Force Long ID as String
                        log.info("📝 Indexing product with ID: " + elasticId + " - " + product.getName()); // 🔥 Log each indexed product
                        return new IndexQueryBuilder()
                            .withId(elasticId)
                            .withObject(product)
                            .build();
                    })
                    .collect(Collectors.toList());
    
                log.info("📤 Sending batch " + batchNumber + " to Elasticsearch (" + indexQueries.size() + " products)");
                elasticsearchOperations.bulkIndex(indexQueries, IndexCoordinates.of(INDEX_NAME));
                log.info("✅ Successfully indexed batch " + batchNumber + " of " + totalBatches);
            }
            
            indexingComplete = true;
            log.info("🎉 All products indexed successfully in Elasticsearch with correct IDs.");
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Failed to index products in Elasticsearch", e);
            throw new RuntimeException("Failed to index products during application startup", e);
        }
    }
    

}
