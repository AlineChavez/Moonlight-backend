package com.moonlight.repository;

import com.moonlight.model.Product;
import com.moonlight.persistence.JpaUtil;

import java.util.List;
import java.util.Optional;

public class JpaProductRepository implements ProductRepository {

    @Override
    public List<Product> findAll() {
        return JpaUtil.doInTransaction(em ->
                em.createQuery("SELECT p FROM Product p", Product.class).getResultList());
    }

    @Override
    public List<Product> findByCategory(String category) {
        return JpaUtil.doInTransaction(em ->
                em.createQuery("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category)", Product.class)
                        .setParameter("category", category)
                        .getResultList());
    }

    @Override
    public List<Product> findFeatured() {
        return JpaUtil.doInTransaction(em ->
                em.createQuery("SELECT p FROM Product p WHERE p.featured = true", Product.class).getResultList());
    }

    @Override
    public Optional<Product> findById(Long id) {
        return JpaUtil.doInTransaction(em -> Optional.ofNullable(em.find(Product.class, id)));
    }

    @Override
    public List<String> findCategories() {
        return JpaUtil.doInTransaction(em ->
                em.createQuery("SELECT DISTINCT p.category FROM Product p", String.class).getResultList());
    }

    @Override
    public Product save(Product product) {
        return JpaUtil.doInTransaction(em -> {
            if (product.getId() == null) {
                em.persist(product);
                return product;
            }
            return em.merge(product);
        });
    }

    @Override
    public boolean deleteById(Long id) {
        return JpaUtil.doInTransaction(em -> {
            Product product = em.find(Product.class, id);
            if (product == null) {
                return false;
            }
            em.remove(product);
            return true;
        });
    }
}
