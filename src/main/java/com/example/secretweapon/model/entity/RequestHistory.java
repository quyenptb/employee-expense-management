package com.example.secretweapon.model.entity;

import com.example.secretweapon.model.enums.HistoryAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_history")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ExpenseRequest expenseRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryAction action;

    @Column(name = "HISTORY_COMMENT")
    private String comment; // note ghi reject expense request (EPIC 03)

    @CreatedDate
    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;

    public RequestHistory(ExpenseRequest expenseRequest, User actor, HistoryAction action, String comment) {
        this.expenseRequest = expenseRequest;
        this.actor = actor;
        this.action = action;
        this.comment = comment;
    }
}
