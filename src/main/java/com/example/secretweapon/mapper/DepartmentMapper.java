package com.example.secretweapon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.payload.request.DepartmentRequest;
import com.example.secretweapon.payload.response.DepartmentResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {
    @Mapping(source = "manager.fullName", target = "managerName")
    DepartmentResponse toResponse(Department department);

    @Mapping(target = "manager", ignore = true)
    Department toEntity(DepartmentRequest request);
}