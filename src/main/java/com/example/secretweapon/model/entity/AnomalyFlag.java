package com.example.secretweapon.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnomalyFlag {
    //id, request_id, rule_key, score, details(json), created_at
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id") 
   private Long id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "expense_request_id")
   private ExpenseRequest expenseRequest;

   @OneToOne
   @JoinColumn(name = "rule_id")
   private Rule rule;

   private Integer score;

   @Column(columnDefinition = "json")
   private String details;

   @CreationTimestamp
   private LocalDateTime createdAt;
  
}

