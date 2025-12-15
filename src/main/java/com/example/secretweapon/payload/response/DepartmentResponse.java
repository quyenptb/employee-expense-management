package com.example.secretweapon.payload.response;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class DepartmentResponse {
        private Long id;
        private String name;
        private Long parentId;
        private String managerName;
    }