package com.example.secretweapon.model.entity;

import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.enums.Period;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    //id, name, role (nullable), job_title (nullable), 
    // project_id (nullable), limit_amount, limit_count_per_period, 
    // period (DAY/WEEK/MONTH), require_special_approval(boolean), priority, enabled
    /*
    {
 "id": 001,
 "name": "rule_for_ba",
 "role": "EMPLOYEE",
 "job_title": "BA",
 "project_id": 001,
 "limit_amount": 2000000,
 "limit_count_per_period": 5000000,
 "period": "MONTH",
 "require_special_approval": "true",
 "priority": 1,
 "enabled": "true"
}
    */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id") // DB sẽ lưu role_id
    private Role role;

    @Enumerated(EnumType.STRING)
    private JobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private BigDecimal limitAmount;

    private Integer limitCountPerPeriod;

    @Enumerated(EnumType.STRING)
    private Period period;

    private Boolean requireSpecialApproval;

    private Integer priority;

    private Boolean enabled;








    







    

}
