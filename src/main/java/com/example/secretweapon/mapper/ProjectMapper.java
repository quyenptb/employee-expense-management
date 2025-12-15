package com.example.secretweapon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.payload.request.ProjectRequest;
import com.example.secretweapon.payload.response.ProjectResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    @Mapping(source = "manager", target = "manager")
    ProjectResponse toResponse(Project project);

    @Mapping(target = "manager", ignore = true)
    Project toEntity(ProjectRequest request);
}