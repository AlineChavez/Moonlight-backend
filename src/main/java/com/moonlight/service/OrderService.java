package com.moonlight.service;

import com.moonlight.exception.ApiException;
import com.moonlight.model.Order;
import com.moonlight.model.OrderItem;
import com.moonlight.repository.OrderRepository;

import java.time.Instant;
import java.util.List;

public class OrderService {

    private static final int MAX_QUANTITY_PER_ITEM = 20;
    private static final List<String> VALID_STATUSES =
            List.of("PENDING", "PREPARING", "READY", "DELIVERED", "CANCELLED");

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order create(Long userId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw ApiException.badRequest("El pedido debe tener al menos un producto");
        }
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0 || item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                throw ApiException.badRequest(
                        "La cantidad de \"" + item.getProductName() + "\" debe estar entre 1 y " + MAX_QUANTITY_PER_ITEM);
            }
        }

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        Order order = new Order(null, userId, items, "PENDING", total, Instant.now().toString());
        Order saved = orderRepository.save(order);

        saved.getItems().forEach(item -> item.setOrderId(saved.getId()));

        return saved;
    }

    public List<Order> getMyOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Pedido no encontrado"));
    }

    public Order updateStatus(Long id, String status) {
        Order order = getById(id);
        if (!VALID_STATUSES.contains(status)) {
            throw ApiException.badRequest("Estado inválido");
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order cancel(Long userId, Long id) {
        Order order = getById(id);
        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("No tienes permiso para cancelar este pedido");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw ApiException.badRequest("Solo se pueden cancelar pedidos pendientes");
        }
        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }
}