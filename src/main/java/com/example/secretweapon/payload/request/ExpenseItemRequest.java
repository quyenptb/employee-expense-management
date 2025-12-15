package com.example.secretweapon.payload.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.secretweapon.model.enums.ExpenseType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
    public class ExpenseItemRequest {
        private Long id; // Null if new, present if update
        @NotNull
        private ExpenseType itemType;
        @NotNull @Positive
        private BigDecimal amount;
        private String description;
        private String receiptUrl;
        @NotNull
        private LocalDateTime incurredDate;
    }
