package com.example.secretweapon.payload.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
    public class ProjectRequest {
        @NotBlank
        private String name;
        @NotNull
        private LocalDateTime startDate;
        @NotNull
        private LocalDateTime endDate;
        @NotNull @PositiveOrZero
        private BigDecimal budgetTotal;
        @NotNull
        private Long managerId;
        private String metadata;
    }