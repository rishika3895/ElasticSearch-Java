package com.example.searchcomparison.controller;

import com.example.searchcomparison.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final ProductService productService;

    @Autowired
    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "false") boolean fuzzy,
            Model model) {
        // Pass !fuzzy as strict parameter (when fuzzy is true, strict should be false and vice versa)
        var results = productService.searchProducts(query, page, size, !fuzzy);
        model.addAttribute("traditionalResults", results.get("traditionalResults"));
        model.addAttribute("elasticResults", results.get("elasticResults"));
        model.addAttribute("traditionalSearchTime", results.get("traditionalSearchTime"));
        model.addAttribute("elasticSearchTime", results.get("elasticSearchTime"));
        model.addAttribute("currentPage", results.get("currentPage"));
        model.addAttribute("totalPages", results.get("totalPages"));
        model.addAttribute("totalElements", results.get("totalElements"));
        model.addAttribute("query", query);
        model.addAttribute("fuzzy", fuzzy);
        model.addAttribute("message", results.get("message"));
        return "search";
    }
    
    @GetMapping("/advanced-search")
    public String advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "false") boolean fuzzy,
            Model model) {
        
        StringBuilder searchQuery = new StringBuilder();
        if (query != null && !query.trim().isEmpty()) {
            searchQuery.append(query.trim());
        }
        if (minPrice != null) {
            if (searchQuery.length() > 0) searchQuery.append(" ");
            searchQuery.append("price>=" + minPrice);
        }
        if (maxPrice != null) {
            if (searchQuery.length() > 0) searchQuery.append(" ");
            searchQuery.append("price<=" + maxPrice);
        }
        if (category != null && !category.trim().isEmpty()) {
            if (searchQuery.length() > 0) searchQuery.append(" ");
            searchQuery.append("category:" + category.trim());
        }
        
        var results = productService.searchProducts(searchQuery.toString(), page, size, !fuzzy);
        
        model.addAttribute("traditionalResults", results.get("traditionalResults"));
        model.addAttribute("elasticResults", results.get("elasticResults"));
        model.addAttribute("traditionalSearchTime", results.get("traditionalSearchTime"));
        model.addAttribute("elasticSearchTime", results.get("elasticSearchTime"));
        model.addAttribute("currentPage", results.get("currentPage"));
        model.addAttribute("totalPages", results.get("totalPages"));
        model.addAttribute("totalElements", results.get("totalElements"));
        model.addAttribute("query", query);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("category", category);
        model.addAttribute("fuzzy", fuzzy);
        model.addAttribute("message", results.get("message"));
        
        return "advanced-search";
    }
}