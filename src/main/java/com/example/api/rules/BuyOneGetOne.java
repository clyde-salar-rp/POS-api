package com.example.api.rules;

import com.example.api.model.BasketItem;
import java.util.*;
import java.util.stream.Collectors;

public class BuyOneGetOne implements DiscountRule {
    private final String category;

    public BuyOneGetOne(String category) {
        this.category = category;
    }

    @Override
    public String getName() {
        return "BOGO_" + category;
    }

    @Override
    public DiscountResult apply(List<BasketItem> items) {
        List<BasketItem> eligible = items.stream()
                .filter(item -> category.equals(item.getCategory()))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            return new DiscountResult(0, "", List.of());
        }

        // Sort by price (discount cheapest items)
        eligible.sort(Comparator.comparingDouble(BasketItem::getPrice));

        double discount = 0;
        List<String> affected = new ArrayList<>();

        for (BasketItem item : eligible) {
            int pairs = item.getQuantity() / 2;
            if (pairs > 0) {
                discount += pairs * item.getPrice();
                affected.add(item.getDescription());
            }
        }

        return new DiscountResult(
                discount,
                "Buy One Get One on " + category,
                affected
        );
    }
}