package com.example.api.repository;

import com.example.api.entity.DiscountRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiscountRuleRepository extends JpaRepository<DiscountRuleEntity, Long> {

    List<DiscountRuleEntity> findByActiveTrue();

    List<DiscountRuleEntity> findByActiveTrueOrderByPriorityDesc();

    boolean existsByName(String name);
}