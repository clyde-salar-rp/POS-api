package com.example.api;

import com.example.api.model.*;
import com.example.api.rules.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DiscountEngineTest {

    @Test
    void testBuyOneGetOne() {
        DiscountEngine engine = new DiscountEngine()
                .addRule(new BuyOneGetOne("BEVERAGE"));

        DiscountRequest request = new DiscountRequest(List.of(
                new BasketItem("001", "Coke", 2.00, 2, "BEVERAGE")
        ));

        DiscountResponse response = engine.calculate(request);

        assertEquals(2.0, response.getTotalDiscount()); // 1 free item
        assertEquals(2.0, response.getSubtotal()); // Only pay for 1
    }

    @Test
    void testPercentOff() {
        DiscountEngine engine = new DiscountEngine()
                .addRule(new PercentOff(10, "FOOD"));

        DiscountRequest request = new DiscountRequest(List.of(
                new BasketItem("002", "Pizza", 10.00, 1, "FOOD")
        ));

        DiscountResponse response = engine.calculate(request);

        assertEquals(1.0, response.getTotalDiscount()); // 10% off
        assertEquals(9.0, response.getSubtotal());
    }
}