package com.example.secretweapon.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {
        @NotBlank
        private String name;
        private Long parentId;
        private Long managerId;
    }
