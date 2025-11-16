package com.example.secretweapon.model.dto;

import lombok.Data;

// DTO trả về thông tin User (ẩn mật khẩu)
@Data
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String managerName;
}