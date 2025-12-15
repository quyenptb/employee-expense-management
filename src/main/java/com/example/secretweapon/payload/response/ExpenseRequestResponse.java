package com.example.secretweapon.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.Currency;
import com.example.secretweapon.model.enums.ExpenseStatus;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class ExpenseRequestResponse {
        private Long id;
        private String requestNo;
        private String title;
        private String description;
        private BigDecimal amountTotal;
        private Currency currency;
        private ExpenseStatus status;
        private Boolean hasSpecialApproval;
        private String specialApprovalReason;
        private UserSummary requester;
        private String projectName;
        private Boolean isAnomalous;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ExpenseItemResponse> items;
        private List<RequestHistoryResponse> history;
    }
