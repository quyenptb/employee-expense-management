package com.example.secretweapon.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.secretweapon.model.enums.DecisionType;
import com.example.secretweapon.model.enums.RoleName;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Approval {
    //id, request_id, approver_id, approver_role, decision (APPROVED/REJECTED/ESCALATED), 
    // comment, created_at

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "expense_request_id")
    private ExpenseRequest expenseRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "approver_id")
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role_name")
    private RoleName approverRole;

    @Enumerated(EnumType.STRING)
    private DecisionType decision;

    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;
    







    
}
