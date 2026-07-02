package com.moonlight.service;

import com.moonlight.exception.ApiException;
import com.moonlight.model.Product;
import com.moonlight.repository.ProductRepository;

import java.util.List;

public class ProductService {

    private static final double MAX_PRICE = 10_000.0;

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
                .orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
    }

    public List<Product> getFeatured() {
        return productRepository.findFeatured();
    }

    public List<String> getCategories() {
        return productRepository.findCategories();
    }

    public Product save(Product product) {
        validate(product);
        // A client-supplied id here would let a create request silently overwrite
        // an unrelated existing product, so creation always assigns a fresh id.
        product.setId(null);
        return productRepository.save(product);
    }

    public Product update(Long id, Product product) {
        getById(id);
        validate(product);
        product.setId(id);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        if (!productRepository.deleteById(id)) {
            throw ApiException.notFound("Producto no encontrado");
        }
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw ApiException.badRequest("El nombre del producto es requerido");
        }
        if (product.getPrice() <= 0) {
            throw ApiException.badRequest("El precio debe ser mayor a 0");
        }
        if (product.getPrice() > MAX_PRICE) {
            throw ApiException.badRequest("El precio no puede superar " + MAX_PRICE);
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw ApiException.badRequest("La categoría del producto es requerida");
        }
    }
}