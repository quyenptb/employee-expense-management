package com.example.secretweapon.model.entity;

import com.example.secretweapon.model.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@EntityListeners(AuditingEntityListener.class) // Bật tự động điền ngày giờ
public class ExpenseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người tạo request (EPIC 02)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(nullable = false)
    private String title; // Mục đích (EPIC 02)

    private String description;

    @Column(nullable = true)
    private Boolean isAnomal;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Số tiền (EPIC 02)

    // Lưu URL của ảnh hóa đơn (EPIC 02). Việc upload file sẽ xử lý ở tầng khác.
    private String receiptImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.DRAFT; // Trạng thái (EPIC 05)

    @CreatedDate
    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // Quan hệ với lịch sử (EPIC 05)
    @OneToMany(mappedBy = "expenseRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequestHistory> history = new ArrayList<>();

    // Helper method để đồng bộ 2 chiều
    public void addHistory(RequestHistory historyEntry) {
        history.add(historyEntry);
        historyEntry.setExpenseRequest(this);
    }
}