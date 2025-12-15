package com.example.secretweapon.mapper;

import com.example.secretweapon.model.entity.Approval;
import com.example.secretweapon.model.entity.ExpenseItem;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.RequestHistory;
import com.example.secretweapon.payload.request.ExpenseItemRequest;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import com.example.secretweapon.payload.response.ApprovalHistoryResponse;
import com.example.secretweapon.payload.response.ExpenseItemResponse;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.payload.response.RequestHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExpenseMapper {
    
    @Mapping(source = "requester", target = "requester")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "expenseItems", target = "items")
    @Mapping(source = "history", target = "history") 
    ExpenseRequestResponse toResponse(ExpenseRequest entity);

    
    ExpenseItemResponse toItemResponse(ExpenseItem entity);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "expenseItems", source = "items")
    ExpenseRequest toEntity(ExpenseRequestCreate request);

    
    ExpenseItem toItemEntity(ExpenseItemRequest request);


    @Mapping(source = "actor.fullName", target = "actorName")
    RequestHistoryResponse toHistoryResponse(RequestHistory history);
    

    @Mapping(source = "approver.fullName", target = "approverName")
    @Mapping(source = "approverRole", target = "approverRole") 
    ApprovalHistoryResponse toApprovalResponse(Approval approval);
}