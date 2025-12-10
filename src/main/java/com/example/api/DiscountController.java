package com.example.api;

import com.example.api.model.*;
import org.springframework.web.bind.annotation.*;
import com.example.api.rules.*;

@RestController
@RequestMapping("/discount")
@CrossOrigin(origins = "*")
public class DiscountController {

    private final DiscountEngine engine;

    public DiscountController() {
        this.engine = new DiscountEngine()
                // Fountain drinks: Buy 2, Get 1 Free
                // Polar Pop costs $0.89-$1.09, 90%+ profit margin
                .addRule(new BuyXGetY(2, 1, "POLAR POP"))

                // Hot food bundle discount: 20% off when buying $8+ of food
                // Encourages hot dog + taquito combos
                .addRule(new PercentOff(5, "FOOD"))

                // Energy drinks: Classic BOGO (buy 1 get 1 free)
                // Applies to Monster, Red Bull, Rockstar, etc.
                .addRule(new BuyOneGetOne("BEVERAGE"));
    }

    @PostMapping
    public DiscountResponse calculate(@RequestBody DiscountRequest request) {
        return engine.calculate(request);
    }
}