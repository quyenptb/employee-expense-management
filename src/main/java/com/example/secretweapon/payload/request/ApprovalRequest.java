package com.example.secretweapon.payload.request;


import com.example.secretweapon.model.enums.DecisionType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO cho Manager/Finance approve/reject (EPIC 03, 04)
@Data
public class ApprovalRequest {
    @NotNull
        private DecisionType decision;
        private String comment;
}