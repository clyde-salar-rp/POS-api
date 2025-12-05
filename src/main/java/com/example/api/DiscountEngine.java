package com.example.api;

import com.example.api.model.*;
import com.example.api.rules.*;
import java.util.*;

public class DiscountEngine {
    private final List<DiscountRule> rules = new ArrayList<>();

    public DiscountEngine addRule(DiscountRule rule) {
        rules.add(rule);
        return this;
    }

    public DiscountResponse calculate(DiscountRequest request) {
        // Calculate original subtotal (sum of all items at full price)
        double originalSubtotal = request.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Track best discount per item (by UPC) for mutually exclusive rules
        Map<String, RuleApplication> bestDiscountPerItem = new HashMap<>();

        // Evaluate each rule
        for (DiscountRule rule : rules) {
            DiscountResult result = rule.apply(request.getItems());

            if (result.getAmount() > 0 && !result.getAffectedItems().isEmpty()) {
                // Store this rule application
                RuleApplication ruleApp = new RuleApplication(
                        rule.getName(),
                        result.getDescription(),
                        result.getAmount(),
                        new ArrayList<>(result.getAffectedItems())
                );

                // For each affected item, check if this rule is better
                for (String itemDesc : result.getAffectedItems()) {
                    BasketItem item = request.getItems().stream()
                            .filter(i -> i.getDescription().equals(itemDesc))
                            .findFirst()
                            .orElse(null);

                    if (item != null) {
                        String key = item.getUpc();

                        // Compare: keep the rule with highest discount
                        if (!bestDiscountPerItem.containsKey(key) ||
                                ruleApp.totalAmount > bestDiscountPerItem.get(key).totalAmount) {
                            bestDiscountPerItem.put(key, ruleApp);
                        }
                    }
                }
            }
        }

        // Collect unique winning rules
        Map<String, AppliedDiscount> winningRules = new HashMap<>();
        double totalDiscount = 0.0;

        for (RuleApplication ruleApp : new HashSet<>(bestDiscountPerItem.values())) {
            winningRules.put(ruleApp.ruleName, new AppliedDiscount(
                    ruleApp.ruleName,
                    ruleApp.description,
                    ruleApp.totalAmount,
                    ruleApp.affectedItems
            ));
            totalDiscount += ruleApp.totalAmount;
        }

        List<AppliedDiscount> applied = new ArrayList<>(winningRules.values());

        // Calculate amount after discount (for tax calculation)
        double amountAfterDiscount = originalSubtotal - totalDiscount;

        // Tax is calculated on the discounted amount
        double tax = amountAfterDiscount * 0.07; // 7% tax

        // Final total = discounted amount + tax
        double total = amountAfterDiscount + tax;

        // RETURN ORIGINAL SUBTOTAL (before discount)
        // This follows retail best practice:
        // Display will show:
        //   SUBTOTAL: $18.54 (original)
        //   DISCOUNT: -$7.12 (savings)
        //   TAX (7%): $0.80  (on $11.42)
        //      TOTAL: $12.22
        return new DiscountResponse(
                originalSubtotal,      // ✅ Original subtotal BEFORE discount
                tax,                   // Tax calculated on discounted amount
                total,                 // Final total (discounted + tax)
                totalDiscount,         // Total discount amount
                applied                // List of applied discounts
        );
    }

    // Helper class to track rule applications
    private static class RuleApplication {
        String ruleName;
        String description;
        double totalAmount;
        List<String> affectedItems;

        RuleApplication(String ruleName, String description, double totalAmount, List<String> affectedItems) {
            this.ruleName = ruleName;
            this.description = description;
            this.totalAmount = totalAmount;
            this.affectedItems = affectedItems;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RuleApplication that = (RuleApplication) o;
            return Objects.equals(ruleName, that.ruleName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ruleName);
        }
    }
}