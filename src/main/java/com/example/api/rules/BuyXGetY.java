package com.example.api.rules;

import com.example.api.model.BasketItem;
import java.util.List;

public class BuyXGetY implements DiscountRule {
    private final int buyQty;
    private final int freeQty;
    private final String itemKeyword;

    public BuyXGetY(int buyQty, int freeQty, String itemKeyword) {
        this.buyQty = buyQty;
        this.freeQty = freeQty;
        this.itemKeyword = itemKeyword.toUpperCase();
    }

    @Override
    public String getName() {
        return "BUY_" + buyQty + "_GET_" + freeQty;
    }

    @Override
    public DiscountResult apply(List<BasketItem> items) {
        BasketItem target = items.stream()
                .filter(item -> item.getDescription().toUpperCase().contains(itemKeyword))
                .findFirst()
                .orElse(null);

        if (target == null || target.getQuantity() < buyQty) {
            return new DiscountResult(0, "", List.of());
        }

        int sets = target.getQuantity() / (buyQty + freeQty);
        double discount = sets * freeQty * target.getPrice();

        return new DiscountResult(
                discount,
                String.format("Buy %d Get %d Free on %s", buyQty, freeQty, itemKeyword),
                List.of(target.getDescription())
        );
    }
}