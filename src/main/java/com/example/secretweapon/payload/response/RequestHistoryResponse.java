package com.example.secretweapon.payload.response;

import java.time.LocalDateTime;

import com.example.secretweapon.model.enums.HistoryAction;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class RequestHistoryResponse {
        private Long id;
        private String actorName;
        private HistoryAction action;
        private String comment;
        private LocalDateTime createdAt;
    }
