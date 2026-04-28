package com.moonlight.service;

import com.moonlight.model.Order;
import com.moonlight.model.OrderItem;
import com.moonlight.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(new OrderRepository());
    }

    private List<OrderItem> mockItems() {
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setProductName("Espresso Clásico");
        item.setSize("medium");
        item.setQuantity(2);
        item.setPrice(12.00);
        return List.of(item);
    }

    @Test
    void create_shouldReturnOrderWithId() {
        Order order = orderService.create(1L, mockItems());
        assertNotNull(order.getId());
        assertEquals(1L, order.getUserId());
        assertEquals("PENDING", order.getStatus());
    }

    @Test
    void create_shouldCalculateTotalCorrectly() {
        Order order = orderService.create(1L, mockItems());
        assertEquals(24.00, order.getTotal(), 0.01);
    }

    @Test
    void create_shouldFailWithEmptyItems() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.create(1L, List.of())
        );
        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    @Test
    void create_shouldFailWithNullItems() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.create(1L, null)
        );
        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    @Test
    void getMyOrders_shouldReturnOrdersForUser() {
        orderService.create(1L, mockItems());
        orderService.create(1L, mockItems());
        orderService.create(2L, mockItems());

        List<Order> ordersUser1 = orderService.getMyOrders(1L);
        assertEquals(2, ordersUser1.size());

        List<Order> ordersUser2 = orderService.getMyOrders(2L);
        assertEquals(1, ordersUser2.size());
    }

    @Test
    void getById_shouldReturnOrder() {
        Order created = orderService.create(1L, mockItems());
        Order found = orderService.getById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void getById_shouldFailIfNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.getById(999L)
        );
        assertEquals("Pedido no encontrado", ex.getMessage());
    }

    @Test
    void updateStatus_shouldChangeStatus() {
        Order order = orderService.create(1L, mockItems());
        Order updated = orderService.updateStatus(order.getId(), "PREPARING");
        assertEquals("PREPARING", updated.getStatus());
    }

    @Test
    void updateStatus_shouldFailWithInvalidStatus() {
        Order order = orderService.create(1L, mockItems());
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.updateStatus(order.getId(), "INVALID")
        );
        assertEquals("Estado inválido", ex.getMessage());
    }
}