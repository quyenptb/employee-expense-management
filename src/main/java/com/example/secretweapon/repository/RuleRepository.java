package com.example.secretweapon.repository;

import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.model.enums.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    @Query("""
        SELECT r FROM Rule r
                WHERE r.enabled = true
                  AND (r.role IS NULL OR r.role = :role)
                  AND (r.jobTitle IS NULL OR r.jobTitle = :jobTitle)
                  AND (r.project IS NULL OR r.project.id = :projectId)
                ORDER BY r.priority ASC
    """)
    List<Rule> findMatchingRules(
            @Param("role") Role role,
            @Param("jobTitle") JobTitle jobTitle,
            @Param("projectId") Long projectId
    );
}
