package com.example.secretweapon.model.dto;


import com.example.secretweapon.model.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// DTO cho Admin tạo user (EPIC 01)
@Data
public class CreateUserRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private RoleName roleName; // EMPLOYEE, MANAGER...

    private Long managerId; // ID của manager (if the role is EMPLOYEE)

    private String avatarUrl;
}