package com.example.secretweapon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.secretweapon.model.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jira_key", unique = true, length = 50)
    private String jiraKey;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "budget_total", precision = 19, scale = 2)
    private BigDecimal budgetTotal;

    @Column(name = "budget_used", precision = 19, scale = 2)
    private BigDecimal budgetUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    private User manager; // owning side

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private ProjectStatus status;

    @Column(columnDefinition = "json")
    private String metadata;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
