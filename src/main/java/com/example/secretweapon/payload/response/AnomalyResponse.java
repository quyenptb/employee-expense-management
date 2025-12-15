package com.example.secretweapon.payload.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class AnomalyResponse {
        private Long id;
        private Long requestId; //Link ra được User
        private String ruleName;
        private Integer score;
        private String details;
        private LocalDateTime createdAt;
    }
