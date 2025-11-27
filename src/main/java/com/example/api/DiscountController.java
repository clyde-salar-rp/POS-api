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
        // Rules configured here
        this.engine = new DiscountEngine()
                .addRule(new BuyOneGetOne("BEVERAGE"))
                .addRule(new PercentOff(10, "FOOD"))
                .addRule(new BuyXGetY(3, 1, "MONSTER"));
    }

    @PostMapping
    public DiscountResponse calculate(@RequestBody DiscountRequest request) {
        return engine.calculate(request);
    }
}