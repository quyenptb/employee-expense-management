package com.example.secretweapon.payload.response;

import com.example.secretweapon.model.enums.UserStatus;

import lombok.Data;

// DTO trả về thông tin User (ẩn mật khẩu)
@Data
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String roleName;
    private String avatarUrl;
    private String jobTitle;
    private String managerName;
    private String departmentName;
    private UserStatus status;
}
