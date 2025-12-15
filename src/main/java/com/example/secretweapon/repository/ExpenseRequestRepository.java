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
    List<ExpenseRequest> findByRequesterOrderByCreatedAtDesc(User requester);

    // Lấy request theo 1 list status (dùng cho Finance - EPIC 04)
    List<ExpenseRequest> findByStatusInOrderByUpdatedAtDesc(List<ExpenseStatus> statuses);

    // Lấy request chờ duyệt của 1 manager (EPIC 03)
    List<ExpenseRequest> findByStatusAndRequester_ManagerOrderByCreatedAtAsc(ExpenseStatus status, User manager);

    // Lấy request theo employee và status
    List<ExpenseRequest> findByRequesterAndStatusOrderByCreatedAtDesc(User employee, ExpenseStatus status);

    @Query("""
       SELECT COUNT(e.id) 
       FROM ExpenseRequest e 
       WHERE e.requester.id = :userId
         AND e.updatedAt >= :startDate
       """)
    Integer countByRequesterSince(@Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate);

    @Query("""
       SELECT COUNT(e.id) 
       FROM ExpenseRequest e 
       WHERE e.requester.id = :userId
         AND e.createdAt BETWEEN :startDate AND :endDate
       """)
    Integer countByRequesterIdAndCreatedAtBetween(@Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate
                                               );


    @Query("""
       SELECT AVG(e.amountTotal)
       FROM ExpenseRequest e
       WHERE e.requester.id = :userId
         AND e.updatedAt BETWEEN :start AND :end
       """)
    BigDecimal findAverageAmountTotalByRequesterAndUpdatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
          );

    @Query("""
        SELECT e FROM ExpenseRequest e
        WHERE (:status IS NULL OR e.status = :status)
          AND (:projectId IS NULL OR e.project.id = :projectId)
          AND (:requesterId IS NULL OR e.requester.id = :requesterId)
          AND (cast(:startDate as timestamp) IS NULL OR e.createdAt >= :startDate)
          AND (cast(:endDate as timestamp) IS NULL OR e.createdAt <= :endDate)
        ORDER BY e.createdAt DESC
    """)
    List<ExpenseRequest> searchRequests(
            @Param("status") ExpenseStatus status,
            @Param("projectId") Long projectId,
            @Param("requesterId") Long requesterId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM ExpenseRequest e WHERE e.requester.manager.id = :managerId AND e.status IN ('PENDING_FINANCE', 'APPROVED', 'PAID', 'MANAGER_REJECTED', 'FINANCE_REJECTED') ORDER BY e.updatedAt DESC")
    List<ExpenseRequest> findHistoryByManager(@Param("managerId") Long managerId);


}

