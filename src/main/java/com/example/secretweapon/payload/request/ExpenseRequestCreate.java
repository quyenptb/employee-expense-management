package com.example.secretweapon.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.example.secretweapon.model.enums.Currency;

// DTO cho Employee tạo request (EPIC 02)
@Data
public class ExpenseRequestCreate {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;
    
        @NotNull
        private Long projectId;
        @NotNull
        private Currency currency;
        
        @NotEmpty
        private List<ExpenseItemRequest> items;
        
        private String attachments;
        private String metadata;
}

