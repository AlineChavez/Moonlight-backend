package com.moonlight.repository;

import com.moonlight.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    List<Product> findByCategory(String category);
    List<Product> findFeatured();
    Optional<Product> findById(Long id);
    List<String> findCategories();
    Product save(Product product);
    boolean deleteById(Long id);
}
