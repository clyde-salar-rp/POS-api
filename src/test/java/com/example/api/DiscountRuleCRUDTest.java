package com.example.api;

import com.example.api.dto.DiscountRuleDTO;
import com.example.api.entity.RuleType;
import com.example.api.repository.DiscountRuleRepository;
import com.example.api.service.DiscountRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DiscountRuleCRUDTest {

    @Autowired
    private DiscountRuleService service;

    @Autowired
    private DiscountRuleRepository repository;

    @Test
    void testCreatePercentOffRule() {
        DiscountRuleDTO dto = new DiscountRuleDTO();
        dto.setName("TEST_PERCENT_OFF");
        dto.setDescription("Test 20% off food");
        dto.setRuleType(RuleType.PERCENT_OFF);
        dto.setPercentOff(20.0);
        dto.setCategory("FOOD");
        dto.setActive(true);
        dto.setPriority(50);

        DiscountRuleDTO created = service.createRule(dto);

        assertNotNull(created.getId());
        assertEquals("TEST_PERCENT_OFF", created.getName());
        assertEquals(20.0, created.getPercentOff());
        assertEquals("FOOD", created.getCategory());
        assertTrue(created.getActive());
    }

    @Test
    void testCreateBuyXGetYRule() {
        DiscountRuleDTO dto = new DiscountRuleDTO();
        dto.setName("TEST_BUY2GET1");
        dto.setDescription("Buy 2 Get 1 Free");
        dto.setRuleType(RuleType.BUY_X_GET_Y);
        dto.setBuyQuantity(2);
        dto.setFreeQuantity(1);
        dto.setItemKeyword("POLAR POP");
        dto.setActive(true);

        DiscountRuleDTO created = service.createRule(dto);

        assertNotNull(created.getId());
        assertEquals(2, created.getBuyQuantity());
        assertEquals(1, created.getFreeQuantity());
        assertEquals("POLAR POP", created.getItemKeyword());
    }

    @Test
    void testGetAllRules() {
        // Create test rules
        createTestRule("RULE_1", RuleType.PERCENT_OFF);
        createTestRule("RULE_2", RuleType.BUY_ONE_GET_ONE);

        List<DiscountRuleDTO> rules = service.getAllRules();

        // Should include default rules + 2 test rules
        assertTrue(rules.size() >= 2);
    }

    @Test
    void testGetActiveRulesOnly() {
        // Create active rule
        DiscountRuleDTO activeRule = createTestRule("ACTIVE_RULE", RuleType.PERCENT_OFF);

        // Create inactive rule
        DiscountRuleDTO inactiveRule = createTestRule("INACTIVE_RULE", RuleType.PERCENT_OFF);
        service.toggleActive(inactiveRule.getId());

        List<DiscountRuleDTO> activeRules = service.getActiveRules();

        assertTrue(activeRules.stream().anyMatch(r -> r.getName().equals("ACTIVE_RULE")));
        assertFalse(activeRules.stream().anyMatch(r -> r.getName().equals("INACTIVE_RULE")));
    }

    @Test
    void testUpdateRule() {
        DiscountRuleDTO created = createTestRule("UPDATE_TEST", RuleType.PERCENT_OFF);

        created.setDescription("Updated description");
        created.setPercentOff(30.0);

        DiscountRuleDTO updated = service.updateRule(created.getId(), created);

        assertEquals("Updated description", updated.getDescription());
        assertEquals(30.0, updated.getPercentOff());
    }

    @Test
    void testToggleActive() {
        DiscountRuleDTO created = createTestRule("TOGGLE_TEST", RuleType.PERCENT_OFF);
        assertTrue(created.getActive());

        DiscountRuleDTO toggled = service.toggleActive(created.getId());
        assertFalse(toggled.getActive());

        DiscountRuleDTO toggledAgain = service.toggleActive(created.getId());
        assertTrue(toggledAgain.getActive());
    }

    @Test
    void testDeleteRule() {
        DiscountRuleDTO created = createTestRule("DELETE_TEST", RuleType.PERCENT_OFF);
        Long id = created.getId();

        service.deleteRule(id);

        assertThrows(IllegalArgumentException.class, () -> service.getRuleById(id));
    }

    @Test
    void testValidationPercentOffRange() {
        DiscountRuleDTO dto = new DiscountRuleDTO();
        dto.setName("INVALID_PERCENT");
        dto.setDescription("Invalid percent");
        dto.setRuleType(RuleType.PERCENT_OFF);
        dto.setPercentOff(150.0); // Invalid: > 100
        dto.setActive(true);

        assertThrows(IllegalArgumentException.class, () -> service.createRule(dto));
    }

    @Test
    void testValidationBuyXGetYRequiredFields() {
        DiscountRuleDTO dto = new DiscountRuleDTO();
        dto.setName("INVALID_BUYXGETY");
        dto.setDescription("Missing fields");
        dto.setRuleType(RuleType.BUY_X_GET_Y);
        // Missing buyQuantity, freeQuantity, itemKeyword
        dto.setActive(true);

        assertThrows(IllegalArgumentException.class, () -> service.createRule(dto));
    }

    @Test
    void testDuplicateNameValidation() {
        createTestRule("DUPLICATE_NAME", RuleType.PERCENT_OFF);

        assertThrows(IllegalArgumentException.class,
                () -> createTestRule("DUPLICATE_NAME", RuleType.PERCENT_OFF));
    }

    @Test
    void testPriorityOrdering() {
        DiscountRuleDTO low = createTestRule("LOW_PRIORITY", RuleType.PERCENT_OFF);
        low.setPriority(10);
        service.updateRule(low.getId(), low);

        DiscountRuleDTO high = createTestRule("HIGH_PRIORITY", RuleType.PERCENT_OFF);
        high.setPriority(100);
        service.updateRule(high.getId(), high);

        List<DiscountRuleDTO> active = service.getActiveRules();

        // Find our test rules
        int highIndex = -1, lowIndex = -1;
        for (int i = 0; i < active.size(); i++) {
            if (active.get(i).getName().equals("HIGH_PRIORITY")) highIndex = i;
            if (active.get(i).getName().equals("LOW_PRIORITY")) lowIndex = i;
        }

        // High priority should come before low priority
        assertTrue(highIndex < lowIndex);
    }

    // Helper method
    private DiscountRuleDTO createTestRule(String name, RuleType type) {
        DiscountRuleDTO dto = new DiscountRuleDTO();
        dto.setName(name);
        dto.setDescription("Test rule");
        dto.setRuleType(type);
        dto.setActive(true);
        dto.setPriority(50);

        switch (type) {
            case PERCENT_OFF:
                dto.setPercentOff(10.0);
                dto.setCategory("FOOD");
                break;
            case BUY_ONE_GET_ONE:
                dto.setCategory("BEVERAGE");
                break;
            case BUY_X_GET_Y:
                dto.setBuyQuantity(2);
                dto.setFreeQuantity(1);
                dto.setItemKeyword("TEST");
                break;
        }

        return service.createRule(dto);
    }
}