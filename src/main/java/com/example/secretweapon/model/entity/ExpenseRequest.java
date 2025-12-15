package com.example.secretweapon.model.entity;


import com.example.secretweapon.model.enums.Currency;
import com.example.secretweapon.model.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // Bật tự động điền ngày giờ
public class ExpenseRequest {

    // id, request_no, requester_id, project_id, amount_total, currency, state, 
    // created_at, updated_at, description, attachments(json), metadata(json)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_no", unique = true, nullable = false)
    private String requestNo;

    // Người tạo request (EPIC 02)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountTotal; // Số tiền (EPIC 02)

    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 32)
    private Currency currency; //USD, VNĐ, YEN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.DRAFT; // Trạng thái (EPIC 05)

    @CreatedDate
    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    private String description;

    @Column(columnDefinition = "json")
    private String attachments;

    @Column(columnDefinition = "json")
    private String metadata;

    @Column(nullable = false)
    private String title; // Mục đích (EPIC 02)

    @Column(nullable = true)
    private Boolean isAnomalous;

    private Boolean hasSpecialApproval;

    private String specialApprovalReason;


    // Quan hệ với lịch sử (EPIC 05)
    @OneToMany(mappedBy = "expenseRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequestHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "expenseRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseItem> expenseItems = new ArrayList<>();

    public void addHistory(RequestHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setExpenseRequest(this);
    }

    //Helper owning side (Expense Request) & inversing side (Expense Item)
    public void addExpenseRequest(ExpenseItem expenseItemEntry) {
        expenseItems.add(expenseItemEntry);
        expenseItemEntry.setExpenseRequest(this);
    }
}