package com.moonlight.repository;

import com.moonlight.model.Order;
import com.moonlight.persistence.JpaUtil;

import java.util.List;
import java.util.Optional;

public class JpaOrderRepository implements OrderRepository {

    @Override
    public Order save(Order order) {
        return JpaUtil.doInTransaction(em -> {
            if (order.getId() == null) {
                em.persist(order);
                return order;
            }
            return em.merge(order);
        });
    }

    @Override
    public Optional<Order> findById(Long id) {
        return JpaUtil.doInTransaction(em -> Optional.ofNullable(em.find(Order.class, id)));
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return JpaUtil.doInTransaction(em ->
                em.createQuery("SELECT o FROM Order o WHERE o.userId = :userId", Order.class)
                        .setParameter("userId", userId)
                        .getResultList());
    }

    @Override
    public List<Order> findAll() {
        return JpaUtil.doInTransaction(em -> em.createQuery("SELECT o FROM Order o", Order.class).getResultList());
    }
}
