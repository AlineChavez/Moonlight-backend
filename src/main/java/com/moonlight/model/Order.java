package com.moonlight.model;

import java.util.List;

public class Order {
    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private String status;
    private double total;
    private String createdAt;

    public Order() {}

    public Order(Long id, Long userId, List<OrderItem> items, String status, double total, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}