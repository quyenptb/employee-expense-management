package com.example.secretweapon.payload.request;

import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.enums.UserStatus;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Id
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private Long managerId;
    private String avatarUrl;
    private Long departmentId;
    private JobTitle jobTitle;
    private UserStatus status;
}
