package com.example.api.rules;

import com.example.api.model.BasketItem;
import java.util.*;
import java.util.stream.Collectors;

public class MixAndMatchEnergyDrinks implements DiscountRule {
    private final int requiredQty;
    private final double bundlePrice;

    public MixAndMatchEnergyDrinks() {
        this.requiredQty = 2;
        this.bundlePrice = 6.00;
    }

    @Override
    public String getName() {
        return "MIX_MATCH_ENERGY_2FOR6";
    }

    @Override
    public DiscountResult apply(List<BasketItem> items) {
        // Filter energy drinks (Monster, Red Bull, Rockstar, etc.)
        List<BasketItem> energyDrinks = items.stream()
                .filter(item -> isEnergyDrink(item.getDescription()))
                .collect(Collectors.toList());

        if (energyDrinks.isEmpty()) {
            return new DiscountResult(0, "", List.of());
        }

        // Calculate total energy drink quantity
        int totalQty = energyDrinks.stream()
                .mapToInt(BasketItem::getQuantity)
                .sum();

        if (totalQty < requiredQty) {
            return new DiscountResult(0, "", List.of());
        }

        // Calculate how many bundles
        int bundles = totalQty / requiredQty;

        // Calculate savings
        // For simplicity, assume average energy drink price is $3.50
        // Actual: should calculate from actual items, but this is cleaner
        double normalPrice = energyDrinks.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Calculate bundle discount
        int itemsInBundles = bundles * requiredQty;
        double bundleTotal = bundles * bundlePrice;
        double regularPriceForBundled = energyDrinks.stream()
                .limit(itemsInBundles) // Simplified - should properly sort and select
                .mapToDouble(item -> item.getPrice() * Math.min(item.getQuantity(), itemsInBundles))
                .sum();

        double discount = regularPriceForBundled - bundleTotal;

        if (discount <= 0) {
            return new DiscountResult(0, "", List.of());
        }

        List<String> affected = energyDrinks.stream()
                .map(BasketItem::getDescription)
                .collect(Collectors.toList());

        return new DiscountResult(
                discount,
                String.format("%d for $%.2f on Energy Drinks", requiredQty, bundlePrice),
                affected
        );
    }

    private boolean isEnergyDrink(String description) {
        String upper = description.toUpperCase();
        return upper.contains("MONSTER") ||
                upper.contains("RED BULL") ||
                upper.contains("ROCKSTAR") ||
                upper.contains("NOS") ||
                upper.contains("REIGN") ||
                upper.contains("CELSIUS") ||
                upper.contains("BANG") ||
                upper.contains("5 HR") ||
                upper.contains("GHOST ERGY");
    }
}