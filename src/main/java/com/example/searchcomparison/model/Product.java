package com.example.searchcomparison.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Table(name = "products")
@Document(indexName = "products")
@Setting(settingPath = "es-settings.json")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Field(type = FieldType.Keyword)
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "english")
    private String name;
    
    @Field(type = FieldType.Keyword)
    private String nameKeyword;
    
    @Field(type = FieldType.Text, analyzer = "english")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    public Product() {}
    
    public Product(Long id, String name, String description, String category, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.nameKeyword = name;
    }
    
    public String getNameKeyword() {
        return nameKeyword;
    }
    
    public void setNameKeyword(String nameKeyword) {
        this.nameKeyword = nameKeyword;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
}