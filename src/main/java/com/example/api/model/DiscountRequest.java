package com.example.api.model;

import java.util.List;

public class DiscountRequest {
    private List<BasketItem> items;

    public DiscountRequest() {}

    public DiscountRequest(List<BasketItem> items) {
        this.items = items;
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public void setItems(List<BasketItem> items) {
        this.items = items;
    }
}