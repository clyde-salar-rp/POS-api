package com.example.api.dto;

import com.example.api.entity.RuleType;
import java.time.LocalDateTime;

public class DiscountRuleDTO {
    private Long id;
    private String name;
    private String description;
    private RuleType ruleType;
    private Double percentOff;
    private String category;
    private Integer buyQuantity;
    private Integer freeQuantity;
    private String itemKeyword;
    private Integer requiredQuantity;
    private Double bundlePrice;
    private Boolean active;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DiscountRuleDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }

    public Double getPercentOff() { return percentOff; }
    public void setPercentOff(Double percentOff) { this.percentOff = percentOff; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getBuyQuantity() { return buyQuantity; }
    public void setBuyQuantity(Integer buyQuantity) { this.buyQuantity = buyQuantity; }

    public Integer getFreeQuantity() { return freeQuantity; }
    public void setFreeQuantity(Integer freeQuantity) { this.freeQuantity = freeQuantity; }

    public String getItemKeyword() { return itemKeyword; }
    public void setItemKeyword(String itemKeyword) { this.itemKeyword = itemKeyword; }

    public Integer getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Integer requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public Double getBundlePrice() { return bundlePrice; }
    public void setBundlePrice(Double bundlePrice) { this.bundlePrice = bundlePrice; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}