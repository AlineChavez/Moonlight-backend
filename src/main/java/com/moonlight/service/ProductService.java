package com.moonlight.service;

import com.moonlight.model.Product;
import com.moonlight.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAll(String category) {
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category);
        }
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public List<Product> getFeatured() {
        return productRepository.findFeatured();
    }

    public List<String> getCategories() {
        return productRepository.findCategories();
    }

    public Product save(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new RuntimeException("El nombre del producto es requerido");
        }
        if (product.getPrice() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        return productRepository.save(product);
    }
}