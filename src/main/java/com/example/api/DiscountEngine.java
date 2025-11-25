package com.example.api;

import com.example.api.model.*;
import com.example.api.rules.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountEngine {
    private final List<DiscountRule> rules = new ArrayList<>();

    public DiscountEngine addRule(DiscountRule rule) {
        rules.add(rule);
        return this;
    }

    public DiscountResponse calculate(DiscountRequest request) {
        // Calculate original subtotal (before discount)
        double originalSubtotal = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        List<AppliedDiscount> applied = new ArrayList<>();
        double totalDiscount = 0.0;

        // Apply each rule
        for (DiscountRule rule : rules) {
            DiscountResult result = rule.apply(request.getItems());
            if (result.getAmount() > 0) {
                applied.add(new AppliedDiscount(
                        rule.getName(),
                        result.getDescription(),
                        result.getAmount(),
                        result.getAffectedItems()
                ));
                totalDiscount += result.getAmount();
            }
        }

        // Calculate discounted subtotal (after discount)
        double discountedSubtotal = originalSubtotal - totalDiscount;

        // Tax is calculated on the discounted subtotal
        double tax = discountedSubtotal * 0.07; // 7% tax

        // Final total
        double total = discountedSubtotal + tax;

        // Return original subtotal (not discounted yet)
        return new DiscountResponse(
                originalSubtotal,      // Return original subtotal before discount
                tax,                   // Tax calculated on discounted amount
                total,                 // Final total after discount and tax
                totalDiscount,         // Total discount amount
                applied                // List of applied discounts
        );
    }
}