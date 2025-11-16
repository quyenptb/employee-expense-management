package com.example.secretweapon.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private List<String> roles;
    private Long userId;
}