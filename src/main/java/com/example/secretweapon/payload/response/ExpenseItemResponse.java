package com.example.secretweapon.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.secretweapon.model.enums.ExpenseType;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class ExpenseItemResponse {
        private Long id;
        private ExpenseType itemType;
        private BigDecimal amount;
        private String description;
        private String receiptUrl;
        private LocalDateTime incurredDate;
    }
