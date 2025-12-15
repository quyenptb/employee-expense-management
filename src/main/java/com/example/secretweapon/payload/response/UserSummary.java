package com.example.secretweapon.payload.response;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
    public class UserSummary {
        private Long id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
