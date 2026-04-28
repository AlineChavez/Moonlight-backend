package com.moonlight.service;

import com.moonlight.model.Order;
import com.moonlight.model.OrderItem;
import com.moonlight.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order create(Long userId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("El pedido debe tener al menos un producto");
        }

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        Order order = new Order(null, userId, items, "PENDING", total, LocalDateTime.now());
        Order saved = orderRepository.save(order);

        saved.getItems().forEach(item -> item.setOrderId(saved.getId()));

        return saved;
    }

    public List<Order> getMyOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    public Order updateStatus(Long id, String status) {
        Order order = getById(id);
        List<String> validStatuses = List.of("PENDING", "PREPARING", "READY", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Estado inválido");
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }
}