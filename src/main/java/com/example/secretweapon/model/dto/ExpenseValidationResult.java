package com.example.secretweapon.model.dto;

import com.example.secretweapon.model.enums.RuleDecisionType;
import lombok.Data;

@Data
public class ExpenseValidationResult {
    private boolean isValid = true;
    private RuleDecisionType decision = RuleDecisionType.ALLOW_NORMAL;
    private String reason;
    private Long violatedRuleId;

    public void flagViolation(RuleDecisionType decision, String reason, Long ruleId) {
        this.isValid = false;
        this.decision = decision;
        this.reason = reason;
        this.violatedRuleId = ruleId;
    }
}