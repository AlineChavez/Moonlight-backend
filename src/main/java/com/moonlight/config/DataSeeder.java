package com.moonlight.config;

import com.moonlight.model.Product;
import com.moonlight.model.User;
import com.moonlight.repository.JpaProductRepository;
import com.moonlight.repository.JpaUserRepository;
import com.moonlight.repository.ProductRepository;
import com.moonlight.repository.UserRepository;
import com.moonlight.security.PasswordUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * Runs once at application startup: seeds the sample catalog on a fresh database
 * and bootstraps the initial ADMIN user from ADMIN_EMAIL/ADMIN_PASSWORD, since
 * there's otherwise no way to create the first admin (registration always assigns
 * role USER).
 */
public final class DataSeeder {

    private static final Logger LOGGER = Logger.getLogger(DataSeeder.class.getName());
    private static volatile boolean seeded = false;

    private DataSeeder() {}

    public static synchronized void seedIfNeeded() {
        if (seeded) {
            return;
        }
        seedProducts(new JpaProductRepository());
        seedAdmin(new JpaUserRepository());
        seeded = true;
    }

    private static void seedProducts(ProductRepository productRepository) {
        if (!productRepository.findAll().isEmpty()) {
            return;
        }
        List<Product> sampleProducts = List.of(
                new Product(null, "Espresso Clásico", "Concentrado puro con crema dorada y notas de chocolate oscuro", 12.00, "espresso", true, true),
                new Product(null, "Cappuccino Artesanal", "Espresso, leche vaporizada y espuma sedosa en perfecta armonía", 16.00, "espresso", true, true),
                new Product(null, "Cold Brew Reserve", "Infusión fría por 18 horas, suave y con notas frutales", 18.00, "frio", true, true),
                new Product(null, "Latte de Vainilla", "Espresso suave con leche cremosa y sirope de vainilla artesanal", 17.00, "espresso", false, true),
                new Product(null, "Matcha Latte", "Matcha ceremonial japonés con leche de avena y miel de abeja", 19.00, "especial", false, true),
                new Product(null, "Café de Altura", "Granos seleccionados de 1800 msnm, filtrado por goteo lento", 14.00, "filtrado", false, true),
                new Product(null, "Mocha Oscuro", "Espresso doble con chocolate amargo belga y leche vaporizada", 18.00, "espresso", false, true),
                new Product(null, "Frappé Caramelo", "Base de espresso, hielo, crema y caramelo artesanal", 20.00, "frio", true, true)
        );
        sampleProducts.forEach(productRepository::save);
        LOGGER.info("Catálogo de productos de ejemplo insertado (" + sampleProducts.size() + " productos).");
    }

    private static void seedAdmin(UserRepository userRepository) {
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.length() < 8) {
            LOGGER.warning("ADMIN_EMAIL/ADMIN_PASSWORD no configurados (o contraseña < 8 caracteres): " +
                    "no se creará ningún administrador. El panel de administración quedará inaccesible " +
                    "hasta que se definan esas variables de entorno.");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = new User(null, "Administrador", adminEmail, PasswordUtil.hash(adminPassword), "ADMIN");
        userRepository.save(admin);
        LOGGER.info("Usuario administrador inicial creado desde ADMIN_EMAIL/ADMIN_PASSWORD.");
    }
}
