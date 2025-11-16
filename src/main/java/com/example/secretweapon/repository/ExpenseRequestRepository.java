package com.example.secretweapon.repository;


import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRequestRepository extends JpaRepository<ExpenseRequest, Long> {

    // Lấy request của 1 employee (EPIC 05)
    List<ExpenseRequest> findByEmployeeOrderByCreatedAtDesc(User employee);

    // Lấy request theo 1 list status (dùng cho Finance - EPIC 04)
    List<ExpenseRequest> findByStatusInOrderByUpdatedAtDesc(List<ExpenseStatus> statuses);

    // Lấy request chờ duyệt của 1 manager (EPIC 03)
    List<ExpenseRequest> findByStatusAndEmployee_ManagerOrderByCreatedAtAsc(ExpenseStatus status, User manager);

    // Lấy request theo employee và status
    List<ExpenseRequest> findByEmployeeAndStatusOrderByCreatedAtDesc(User employee, ExpenseStatus status);

    @Query("""
       SELECT COUNT(e.id) 
       FROM ExpenseRequest e 
       WHERE e.employee.id = :userId
         AND e.updatedAt >= :startDate
       """)
    Integer countByEmployeeSince(@Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate);


    @Query("""
       SELECT AVG(e.amount)
       FROM ExpenseRequest e
       WHERE e.employee.id = :userId
         AND e.updatedAt BETWEEN :start AND :end
       """)
    BigDecimal findAverageAmountByEmployeeAndUpdatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


}

