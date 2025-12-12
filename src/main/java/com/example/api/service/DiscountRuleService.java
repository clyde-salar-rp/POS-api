package com.example.api.service;

import com.example.api.dto.DiscountRuleDTO;
import com.example.api.entity.DiscountRuleEntity;
import com.example.api.entity.RuleType;
import com.example.api.repository.DiscountRuleRepository;
import com.example.api.rules.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountRuleService {

    private final DiscountRuleRepository repository;

    public DiscountRuleService(DiscountRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DiscountRuleDTO createRule(DiscountRuleDTO dto) {
        if (repository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Rule with name '" + dto.getName() + "' already exists");
        }

        validateRule(dto);

        DiscountRuleEntity entity = toEntity(dto);
        DiscountRuleEntity saved = repository.save(entity);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<DiscountRuleDTO> getAllRules() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiscountRuleDTO> getActiveRules() {
        return repository.findByActiveTrueOrderByPriorityDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DiscountRuleDTO getRuleById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));
    }

    @Transactional
    public DiscountRuleDTO updateRule(Long id, DiscountRuleDTO dto) {
        DiscountRuleEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));

        validateRule(dto);

        entity.setDescription(dto.getDescription());
        entity.setRuleType(dto.getRuleType());
        entity.setPercentOff(dto.getPercentOff());
        entity.setCategory(dto.getCategory());
        entity.setBuyQuantity(dto.getBuyQuantity());
        entity.setFreeQuantity(dto.getFreeQuantity());
        entity.setItemKeyword(dto.getItemKeyword());
        entity.setRequiredQuantity(dto.getRequiredQuantity());
        entity.setBundlePrice(dto.getBundlePrice());
        entity.setActive(dto.getActive());
        entity.setPriority(dto.getPriority());

        DiscountRuleEntity updated = repository.save(entity);
        return toDTO(updated);
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Rule not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public DiscountRuleDTO toggleActive(Long id) {
        DiscountRuleEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with id: " + id));

        entity.setActive(!entity.getActive());
        DiscountRuleEntity updated = repository.save(entity);
        return toDTO(updated);
    }

    public List<DiscountRule> getActiveDiscountRules() {
        return repository.findByActiveTrueOrderByPriorityDesc().stream()
                .map(this::toDiscountRule)
                .collect(Collectors.toList());
    }

    private void validateRule(DiscountRuleDTO dto) {
        if (dto.getRuleType() == null) {
            throw new IllegalArgumentException("Rule type is required");
        }

        switch (dto.getRuleType()) {
            case PERCENT_OFF:
                if (dto.getPercentOff() == null || dto.getPercentOff() <= 0 || dto.getPercentOff() > 100) {
                    throw new IllegalArgumentException("Percent off must be between 0 and 100");
                }
                break;
            case BUY_ONE_GET_ONE:
                if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
                    throw new IllegalArgumentException("Category is required for BOGO rules");
                }
                break;
            case BUY_X_GET_Y:
                if (dto.getBuyQuantity() == null || dto.getBuyQuantity() <= 0) {
                    throw new IllegalArgumentException("Buy quantity must be positive");
                }
                if (dto.getFreeQuantity() == null || dto.getFreeQuantity() <= 0) {
                    throw new IllegalArgumentException("Free quantity must be positive");
                }
                if (dto.getItemKeyword() == null || dto.getItemKeyword().trim().isEmpty()) {
                    throw new IllegalArgumentException("Item keyword is required for Buy X Get Y rules");
                }
                break;
            case MIX_AND_MATCH:
                if (dto.getRequiredQuantity() == null || dto.getRequiredQuantity() <= 0) {
                    throw new IllegalArgumentException("Required quantity must be positive");
                }
                if (dto.getBundlePrice() == null || dto.getBundlePrice() <= 0) {
                    throw new IllegalArgumentException("Bundle price must be positive");
                }
                break;
        }
    }

    private DiscountRule toDiscountRule(DiscountRuleEntity entity) {
        switch (entity.getRuleType()) {
            case PERCENT_OFF:
                if (entity.getCategory() != null && !entity.getCategory().trim().isEmpty()) {
                    return new PercentOff(entity.getPercentOff(), entity.getCategory());
                } else {
                    return new PercentOff(entity.getPercentOff());
                }
            case BUY_ONE_GET_ONE:
                return new BuyOneGetOne(entity.getCategory());
            case BUY_X_GET_Y:
                return new BuyXGetY(entity.getBuyQuantity(), entity.getFreeQuantity(), entity.getItemKeyword());
            case MIX_AND_MATCH:
                return new MixAndMatchEnergyDrinks();
            default:
                throw new IllegalArgumentException("Unknown rule type: " + entity.getRuleType());
        }
    }

    private DiscountRuleEntity toEntity(DiscountRuleDTO dto) {
        DiscountRuleEntity entity = new DiscountRuleEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setRuleType(dto.getRuleType());
        entity.setPercentOff(dto.getPercentOff());
        entity.setCategory(dto.getCategory());
        entity.setBuyQuantity(dto.getBuyQuantity());
        entity.setFreeQuantity(dto.getFreeQuantity());
        entity.setItemKeyword(dto.getItemKeyword());
        entity.setRequiredQuantity(dto.getRequiredQuantity());
        entity.setBundlePrice(dto.getBundlePrice());
        entity.setActive(dto.getActive() != null ? dto.getActive() : true);
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        return entity;
    }

    private DiscountRuleDTO toDTO(DiscountRuleEntity entity) {
        DiscountRuleDTO dto = new DiscountRuleDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setRuleType(entity.getRuleType());
        dto.setPercentOff(entity.getPercentOff());
        dto.setCategory(entity.getCategory());
        dto.setBuyQuantity(entity.getBuyQuantity());
        dto.setFreeQuantity(entity.getFreeQuantity());
        dto.setItemKeyword(entity.getItemKeyword());
        dto.setRequiredQuantity(entity.getRequiredQuantity());
        dto.setBundlePrice(entity.getBundlePrice());
        dto.setActive(entity.getActive());
        dto.setPriority(entity.getPriority());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}