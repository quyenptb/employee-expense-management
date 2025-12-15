package com.example.secretweapon.mapper;


import com.example.secretweapon.model.entity.*;
import com.example.secretweapon.payload.response.*;
import org.mapstruct.*;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnomalyMapper {
    @Mapping(source = "expenseRequest.id", target = "requestId")
    @Mapping(source = "rule.name", target = "ruleName")
    AnomalyResponse toResponse(AnomalyFlag anomalyFlag);
}