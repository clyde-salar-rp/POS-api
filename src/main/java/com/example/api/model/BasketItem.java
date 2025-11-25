package com.example.api.model;

public class BasketItem {
    private String upc;
    private String description;
    private double price;
    private int quantity;
    private String category;

    public BasketItem() {}

    public BasketItem(String upc, String description, double price,
                      int quantity, String category) {
        this.upc = upc;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}