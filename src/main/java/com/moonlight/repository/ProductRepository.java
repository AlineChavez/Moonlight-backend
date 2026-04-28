package com.moonlight.repository;

import com.moonlight.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ProductRepository {

    private static final List<Product> products = new ArrayList<>();
    private static final AtomicLong counter = new AtomicLong(1);

    static {
        products.add(new Product(counter.getAndIncrement(), "Espresso Clásico", "Concentrado puro con crema dorada y notas de chocolate oscuro", 12.00, "espresso", true, true));
        products.add(new Product(counter.getAndIncrement(), "Cappuccino Artesanal", "Espresso, leche vaporizada y espuma sedosa en perfecta armonía", 16.00, "espresso", true, true));
        products.add(new Product(counter.getAndIncrement(), "Cold Brew Reserve", "Infusión fría por 18 horas, suave y con notas frutales", 18.00, "frio", true, true));
        products.add(new Product(counter.getAndIncrement(), "Latte de Vainilla", "Espresso suave con leche cremosa y sirope de vainilla artesanal", 17.00, "espresso", false, true));
        products.add(new Product(counter.getAndIncrement(), "Matcha Latte", "Matcha ceremonial japonés con leche de avena y miel de abeja", 19.00, "especial", false, true));
        products.add(new Product(counter.getAndIncrement(), "Café de Altura", "Granos seleccionados de 1800 msnm, filtrado por goteo lento", 14.00, "filtrado", false, true));
        products.add(new Product(counter.getAndIncrement(), "Mocha Oscuro", "Espresso doble con chocolate amargo belga y leche vaporizada", 18.00, "espresso", false, true));
        products.add(new Product(counter.getAndIncrement(), "Frappé Caramelo", "Base de espresso, hielo, crema y caramelo artesanal", 20.00, "frio", true, true));
    }

    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public List<Product> findByCategory(String category) {
        return products.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Product> findFeatured() {
        return products.stream()
                .filter(Product::isFeatured)
                .collect(Collectors.toList());
    }

    public Optional<Product> findById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<String> findCategories() {
        return products.stream()
                .map(Product::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(counter.getAndIncrement());
            products.add(product);
        } else {
            products.removeIf(p -> p.getId().equals(product.getId()));
            products.add(product);
        }
        return product;
    }
}