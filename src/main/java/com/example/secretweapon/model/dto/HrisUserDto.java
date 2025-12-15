package com.example.secretweapon.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HrisUserDto {
    private String externalId;
    private String fullName;
    private String email;
    private String jobTitle; // map to JobTitle enum
    private String departmentName; // or externalDepartmentId
    private String managerEmail;
    private String avatarUrl;
}