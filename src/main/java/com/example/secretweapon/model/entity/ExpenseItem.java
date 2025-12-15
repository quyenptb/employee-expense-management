package com.example.secretweapon.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.secretweapon.model.enums.ExpenseType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExpenseItem {
    //id, request_id, item_type, amount, description, receipt_url, incurred_date

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id") // FK nằm ở bảng Items
    private ExpenseRequest expenseRequest;

    @Enumerated(EnumType.STRING)
    private ExpenseType itemType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    private String description;

    @Column(name = "receipt_url", length = 1000) 
    private String receiptUrl;

    private LocalDateTime incurred_date;
    
    
    
}
