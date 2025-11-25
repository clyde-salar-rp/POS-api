package com.example.api.rules;

import com.example.api.model.BasketItem;
import java.util.List;

public interface DiscountRule {
    String getName();
    DiscountResult apply(List<BasketItem> items);
}