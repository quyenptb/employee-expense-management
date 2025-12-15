package com.example.secretweapon.payload.response;

import java.time.LocalDateTime;

import com.example.secretweapon.model.enums.DecisionType;
import com.example.secretweapon.model.enums.RoleName;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class ApprovalHistoryResponse {
        private Long id;
        private String approverName;
        private RoleName approverRole;
        private DecisionType decision;
        private String comment;
        private LocalDateTime createdAt;
    }