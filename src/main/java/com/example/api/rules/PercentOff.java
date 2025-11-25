package com.example.api.rules;

import com.example.api.model.BasketItem;
import java.util.*;
import java.util.stream.Collectors;

public class PercentOff implements DiscountRule {
    private final double percent;
    private final List<String> categories;

    // Single category
    public PercentOff(double percent, String category) {
        this.percent = percent;
        this.categories = Arrays.asList(category);
    }

    // Multiple categories
    public PercentOff(double percent, String... categories) {
        this.percent = percent;
        this.categories = Arrays.asList(categories);
    }

    // All items
    public PercentOff(double percent) {
        this.percent = percent;
        this.categories = List.of();
    }

    @Override
    public String getName() {
        return "PERCENT_OFF_" + (int)percent;
    }

    @Override
    public DiscountResult apply(List<BasketItem> items) {
        double discount = items.stream()
                .filter(item -> categories.isEmpty() || categories.contains(item.getCategory()))
                .mapToDouble(item -> item.getPrice() * item.getQuantity() * (percent / 100))
                .sum();

        List<String> affected = items.stream()
                .filter(item -> categories.isEmpty() || categories.contains(item.getCategory()))
                .map(BasketItem::getDescription)
                .collect(Collectors.toList());

        String desc = categories.isEmpty()
                ? String.format("%.0f%% off everything", percent)
                : String.format("%.0f%% off %s", percent, String.join(", ", categories));

        return new DiscountResult(discount, desc, affected);
    }
}