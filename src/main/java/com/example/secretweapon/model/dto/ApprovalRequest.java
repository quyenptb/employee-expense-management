package com.example.secretweapon.model.dto;


import lombok.Data;

// DTO cho Manager/Finance approve/reject (EPIC 03, 04)
@Data
public class ApprovalRequest {
    private String comment;
}