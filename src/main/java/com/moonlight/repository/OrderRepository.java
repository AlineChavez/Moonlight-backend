package com.moonlight.repository;

import com.moonlight.model.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class OrderRepository {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(counter.getAndIncrement());
            orders.add(order);
        } else {
            orders.removeIf(o -> o.getId().equals(order.getId()));
            orders.add(order);
        }
        return order;
    }

    public Optional<Order> findById(Long id) {
        return orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    public List<Order> findByUserId(Long userId) {
        return orders.stream()
                .filter(o -> o.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }
}