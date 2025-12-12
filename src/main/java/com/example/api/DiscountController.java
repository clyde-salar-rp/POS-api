package com.example.api;

import com.example.api.model.*;
import com.example.api.service.DiscountRuleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discount")
@CrossOrigin(origins = "*")
public class DiscountController {

    private final DiscountRuleService ruleService;

    public DiscountController(DiscountRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public DiscountResponse calculate(@RequestBody DiscountRequest request) {
        DiscountEngine engine = new DiscountEngine();
        ruleService.getActiveDiscountRules().forEach(engine::addRule);
        return engine.calculate(request);
    }
}