package com.example.secretweapon.payload.response;

import com.example.secretweapon.model.enums.JobTitle;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class RuleResponse {
        private Long id;
        private String name;
        private String roleName;
        private JobTitle jobTitle;
        private String projectName;
        private Integer limitAmount;
        private Boolean enabled;
        private Integer priority;
    }
