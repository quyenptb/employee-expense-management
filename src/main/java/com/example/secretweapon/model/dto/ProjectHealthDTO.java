package com.example.secretweapon.model.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

    @Data
    @Builder
    public class ProjectHealthDTO {
        private Long projectId;
        private String projectName;
        private BigDecimal budgetTotal;
        private BigDecimal budgetUsed;
        private BigDecimal burnRateDaily;       
        private Integer daysLeftUntilDepletion; 
        private String healthStatus;            // HEALTHY, WARNING, CRITICAL
        private Double percentageUsed;         
    }