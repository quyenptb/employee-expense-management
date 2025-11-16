package com.example.secretweapon.service;

import com.example.secretweapon.repository.ExpenseRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class AnomalyDetectionServiceImpl {



    @Autowired
    private ExpenseRequestRepository expenseRequestRepository;


    private static final Map<String, BigDecimal> ROLE_AMOUNT_THRESHOLD = Map.of(
            "EMPLOYEE", BigDecimal.valueOf(50_000_000),
            "MANAGER", BigDecimal.valueOf(100_000_000)
    );

    private static final Map<String, Integer> ROLE_REQUEST_THRESHOLD = Map.of(
            "EMPLOYEE", 10,
            "MANAGER", 20
    );

    public boolean isAverageAmountValid(BigDecimal amount, String roleName) {
        return amount.compareTo(ROLE_AMOUNT_THRESHOLD.getOrDefault(roleName, BigDecimal.valueOf(Long.MAX_VALUE))) < 0;
    }

    public boolean isRequestCountValid(int count, String roleName) {
        return count < ROLE_REQUEST_THRESHOLD.getOrDefault(roleName, Integer.MAX_VALUE);
    }


    public boolean isUserAnomaly(BigDecimal employeeAmount, int employeeRequestCount, String roleName) {
        return !isRequestCountValid(employeeRequestCount, roleName) || !isAverageAmountValid(employeeAmount, roleName);
    }
}
