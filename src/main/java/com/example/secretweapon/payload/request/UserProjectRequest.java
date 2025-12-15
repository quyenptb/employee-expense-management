package com.example.secretweapon.payload.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
    public class UserProjectRequest {
        @NotNull
        private Long userId;
        @NotNull
        private Long projectId;
        private String roleInProject;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
