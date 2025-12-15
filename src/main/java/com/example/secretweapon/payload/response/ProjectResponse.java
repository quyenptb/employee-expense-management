package com.example.secretweapon.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.secretweapon.model.enums.ProjectStatus;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class ProjectResponse {
        private Long id;
    private String name;
    private String jiraKey;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal budgetTotal;
    private BigDecimal budgetUsed;
    private UserSummary manager;
    private ProjectStatus status;
    private String metadata;
    }