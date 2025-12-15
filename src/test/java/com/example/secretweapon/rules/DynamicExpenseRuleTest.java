package com.example.secretweapon.rules;

import com.example.secretweapon.model.dto.ExpenseValidationResult;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.model.enums.RuleDecisionType;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicExpenseRuleTest {

    @Test
    @DisplayName("when_AmountExceedsLimit_shouldReturnTrue")
    void when_AmountExceedsLimit_shouldReturnTrue() {
        // Arrange
        Rule dbRule = new Rule();
        dbRule.setLimitAmount(new BigDecimal("1000"));
        
        DynamicExpenseRule rule = new DynamicExpenseRule(dbRule, 0);

        ExpenseRequestCreate request = new ExpenseRequestCreate();
        request.setAmount(new BigDecimal("1500"));

        // Act
        boolean result = rule.when(request);

        // Assert
        assertTrue(result, "Should violate rule when amount > limit");
    }

    @Test
    @DisplayName("when_CountExceedsLimit_shouldReturnTrue")
    void when_CountExceedsLimit_shouldReturnTrue() {
        // Arrange
        Rule dbRule = new Rule();
        dbRule.setLimitCountPerPeriod(5);
        
        // Current count is 6 (already > 5)
        DynamicExpenseRule rule = new DynamicExpenseRule(dbRule, 6);

        ExpenseRequestCreate request = new ExpenseRequestCreate();
        request.setAmount(new BigDecimal("100")); // Amount OK

        // Act
        boolean result = rule.when(request);

        // Assert
        assertTrue(result, "Should violate rule when count > limit");
    }

    @Test
    @DisplayName("then_ShouldFlagViolation")
    void then_ShouldFlagViolation() {
        // Arrange
        Rule dbRule = new Rule();
        dbRule.setId(1L);
        dbRule.setLimitAmount(new BigDecimal("1000"));
        
        DynamicExpenseRule rule = new DynamicExpenseRule(dbRule, 0);
        ExpenseValidationResult validationResult = new ExpenseValidationResult();

        // Act
        rule.then(validationResult);

        // Assert
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getReason().contains("Exceeded limit amount"));
        assertEquals(RuleDecisionType.NEEDS_SPECIAL_APPROVAL, validationResult.getDecision());
        assertEquals(1L, validationResult.getViolatedRuleId());
    }
}