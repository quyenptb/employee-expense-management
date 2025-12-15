package com.example.secretweapon.payload.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectUpdateBudgetRequest {
    private BigDecimal budgetTotal;
    private Long managerId; 
}