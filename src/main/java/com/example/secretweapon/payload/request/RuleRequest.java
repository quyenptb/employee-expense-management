package com.example.secretweapon.payload.request;

import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.enums.Period;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
    public class RuleRequest {
        @NotBlank
        private String name;
        private Long roleId; // Nullable
        private JobTitle jobTitle; // Nullable
        private Long projectId; // Nullable
        private Integer limitAmount;
        private Integer limitAmountPerPeriod;
        private Period period;
        private Boolean requireSpecialApproval;
        private Integer priority;
        private Boolean enabled;
    }