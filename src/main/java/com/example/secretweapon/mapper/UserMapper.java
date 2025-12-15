package com.example.secretweapon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.UserCreateRequest;
import com.example.secretweapon.payload.response.UserResponse;
import com.example.secretweapon.payload.response.UserSummary;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "manager.fullName", target = "managerName")
    UserResponse toResponse(User user);

    UserSummary toSummary(User user);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "manager", ignore = true)
    User toEntity(UserCreateRequest request);
}