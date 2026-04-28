package com.moonlight.model;

public class Product {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String category;
    private boolean featured;
    private boolean available;

    public Product() {}

    public Product(Long id, String name, String description, double price, String category, boolean featured, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.featured = featured;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}