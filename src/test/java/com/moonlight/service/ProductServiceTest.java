package com.moonlight.service;

import com.moonlight.model.Product;
import com.moonlight.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(new ProductRepository());
    }

    @Test
    void getAll_shouldReturnAllProducts() {
        List<Product> products = productService.getAll(null);
        assertFalse(products.isEmpty());
        assertEquals(8, products.size());
    }

    @Test
    void getAll_shouldFilterByCategory() {
        List<Product> espressos = productService.getAll("espresso");
        assertFalse(espressos.isEmpty());
        espressos.forEach(p -> assertEquals("espresso", p.getCategory()));
    }

    @Test
    void getAll_shouldReturnEmptyForUnknownCategory() {
        List<Product> result = productService.getAll("desconocido");
        assertTrue(result.isEmpty());
    }

    @Test
    void getById_shouldReturnProduct() {
        Product product = productService.getById(1L);
        assertNotNull(product);
        assertEquals(1L, product.getId());
    }

    @Test
    void getById_shouldFailIfNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                productService.getById(999L)
        );
        assertEquals("Producto no encontrado", ex.getMessage());
    }

    @Test
    void getFeatured_shouldReturnOnlyFeatured() {
        List<Product> featured = productService.getFeatured();
        assertFalse(featured.isEmpty());
        featured.forEach(p -> assertTrue(p.isFeatured()));
    }

    @Test
    void getCategories_shouldReturnDistinctCategories() {
        List<String> categories = productService.getCategories();
        assertFalse(categories.isEmpty());
        assertEquals(categories.size(), categories.stream().distinct().count());
    }

    @Test
    void save_shouldFailIfNameIsEmpty() {
        Product product = new Product(null, "", "desc", 10.0, "espresso", false, true);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                productService.save(product)
        );
        assertEquals("El nombre del producto es requerido", ex.getMessage());
    }

    @Test
    void save_shouldFailIfPriceIsZero() {
        Product product = new Product(null, "Café", "desc", 0, "espresso", false, true);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                productService.save(product)
        );
        assertEquals("El precio debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void save_shouldSaveValidProduct() {
        Product product = new Product(null, "Nuevo Café", "desc", 15.0, "especial", false, true);
        Product saved = productService.save(product);
        assertNotNull(saved.getId());
        assertEquals("Nuevo Café", saved.getName());
    }
}