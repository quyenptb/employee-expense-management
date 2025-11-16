package com.example.secretweapon.model.dto;

import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.RequestHistory;
import com.example.secretweapon.model.enums.ExpenseStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// DTO return a request (EPIC 05)
@Data
public class ExpenseRequestDto {
    private Long id;
    private String employeeName;
    private Long employeeId;
    private String title;
    private String description;
    private BigDecimal amount;
    private String receiptImageUrl;
    private ExpenseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HistoryDto> history;

    // DTO con cho lịch sử
    @Data
    private static class HistoryDto {
        private String actorName;
        private String action;
        private String comment;
        private LocalDateTime createdAt;

        public HistoryDto(RequestHistory history) {
            this.actorName = history.getActor().getFullName();
            this.action = history.getAction().name();
            this.comment = history.getComment();
            this.createdAt = history.getCreatedAt();
        }
    }

    // Constructor chuyển đổi từ Entity sang DTO
    public ExpenseRequestDto(ExpenseRequest entity) {
        this.id = entity.getId();
        this.employeeName = entity.getEmployee().getFullName();
        this.employeeId = entity.getEmployee().getId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.amount = entity.getAmount();
        this.receiptImageUrl = entity.getReceiptImageUrl();
        this.status = entity.getStatus();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.history = entity.getHistory().stream()
                .map(HistoryDto::new)
                .collect(Collectors.toList());
    }
}
