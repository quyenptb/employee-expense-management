package com.example.secretweapon.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.payload.request.RuleRequest;
import com.example.secretweapon.payload.response.RuleResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RuleMapper {
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "project.name", target = "projectName")
    RuleResponse toResponse(Rule rule);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "project", ignore = true)
    Rule toEntity(RuleRequest request);
}