package com.example.api.rules;

import java.util.List;

public class DiscountResult {
    private final double amount;
    private final String description;
    private final List<String> affectedItems;

    public DiscountResult(double amount, String description, List<String> affectedItems) {
        this.amount = amount;
        this.description = description;
        this.affectedItems = affectedItems;
    }

    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public List<String> getAffectedItems() { return affectedItems; }
}