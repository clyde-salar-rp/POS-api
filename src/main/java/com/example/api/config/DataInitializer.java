package com.example.api.config;

import com.example.api.entity.DiscountRuleEntity;
import com.example.api.entity.RuleType;
import com.example.api.repository.DiscountRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DiscountRuleRepository repository;

    public DataInitializer(DiscountRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            DiscountRuleEntity polarPopRule = new DiscountRuleEntity();
            polarPopRule.setName("POLAR_POP_BUY2GET1");
            polarPopRule.setDescription("Buy 2 Get 1 Free on Polar Pop");
            polarPopRule.setRuleType(RuleType.BUY_X_GET_Y);
            polarPopRule.setBuyQuantity(2);
            polarPopRule.setFreeQuantity(1);
            polarPopRule.setItemKeyword("POLAR POP");
            polarPopRule.setActive(true);
            polarPopRule.setPriority(100);
            repository.save(polarPopRule);

            DiscountRuleEntity foodRule = new DiscountRuleEntity();
            foodRule.setName("FOOD_5_PERCENT");
            foodRule.setDescription("5% off Food Items");
            foodRule.setRuleType(RuleType.PERCENT_OFF);
            foodRule.setPercentOff(5.0);
            foodRule.setCategory("FOOD");
            foodRule.setActive(true);
            foodRule.setPriority(50);
            repository.save(foodRule);

            DiscountRuleEntity beverageRule = new DiscountRuleEntity();
            beverageRule.setName("BEVERAGE_BOGO");
            beverageRule.setDescription("Buy One Get One on Energy Drinks");
            beverageRule.setRuleType(RuleType.BUY_ONE_GET_ONE);
            beverageRule.setCategory("BEVERAGE");
            beverageRule.setActive(true);
            beverageRule.setPriority(75);
            repository.save(beverageRule);

            System.out.println("✅ Initialized database with 3 default discount rules");
        }
    }
}