package com.example.api.model;

import java.util.List;

public class AppliedDiscount {
    private String ruleName;
    private String description;
    private double amount;
    private List<String> affectedItems;

    public AppliedDiscount() {}

    public AppliedDiscount(String ruleName, String description,
                           double amount, List<String> affectedItems) {
        this.ruleName = ruleName;
        this.description = description;
        this.amount = amount;
        this.affectedItems = affectedItems;
    }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public List<String> getAffectedItems() { return affectedItems; }
    public void setAffectedItems(List<String> affectedItems) {
        this.affectedItems = affectedItems;
    }
}