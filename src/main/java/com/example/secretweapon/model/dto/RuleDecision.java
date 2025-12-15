package com.example.secretweapon.model.dto;

import com.example.secretweapon.model.enums.RuleDecisionType;

public record RuleDecision(RuleDecisionType decision, String reason, Long ruleId) {
}
