package com.example.secretweapon.service;



import com.example.secretweapon.exception.AccessDeniedException;
import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.ExpenseMapper;
import com.example.secretweapon.mapper.ProjectMapper;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.RequestHistory;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.model.enums.HistoryAction;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.payload.response.ProjectResponse;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {    
    private final ExpenseRequestRepository expenseRepository;

    private final ProjectRepository projectRepository;

    private final ProjectMapper projectMapper;

    private final ExpenseMapper expenseMapper;

    public List<ExpenseRequestResponse> getTeamPendingRequests(User manager) {
        return expenseRepository.findByStatusAndRequester_ManagerOrderByCreatedAtAsc(
                ExpenseStatus.PENDING_MANAGER,
                manager
        ).stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExpenseRequestResponse approveRequest(Long requestId, User manager, ApprovalRequest approvalRequest) {
        ExpenseRequest request = findRequestAndCheckManager(requestId, manager);

        if (request.getStatus() != ExpenseStatus.PENDING_MANAGER) {
            throw new BadRequestException("Request is not in state PENDING_MANAGER");
        }

        if (request.getHasSpecialApproval() != null && request.getHasSpecialApproval()) {
        // PM duyệt request có cờ đặc biệt -> Chuyển thẳng lên Finance
        request.setStatus(ExpenseStatus.PENDING_FINANCE); 
        request.addHistory(new RequestHistory(
            request, manager, HistoryAction.MANAGER_APPROVED_SPECIAL, approvalRequest.getComment()
        ));
    } else {
        //Normal Flow
        request.setStatus(ExpenseStatus.PENDING_FINANCE);
        request.addHistory(new RequestHistory(
            request, manager, HistoryAction.MANAGER_APPROVED, approvalRequest.getComment()
        ));
    }


        ExpenseRequest savedRequest = expenseRepository.save(request);
        return expenseMapper.toResponse(savedRequest);
    }

    @Transactional
    public ExpenseRequestResponse rejectRequest(Long requestId, User manager, ApprovalRequest approvalRequest) {
        if (approvalRequest.getComment() == null || approvalRequest.getComment().isBlank()) {
            throw new BadRequestException("Phải cung cấp lý do khi từ chối");
        }

        ExpenseRequest request = findRequestAndCheckManager(requestId, manager);

        if (request.getStatus() != ExpenseStatus.PENDING_MANAGER) {
            throw new BadRequestException("Request is not in state PENDING_MANAGER");
        }

        request.setStatus(ExpenseStatus.MANAGER_REJECTED); 
        request.addHistory(new RequestHistory(
                request,
                manager,
                HistoryAction.MANAGER_REJECTED,
                approvalRequest.getComment()
        ));

        ExpenseRequest savedRequest = expenseRepository.save(request);
        return expenseMapper.toResponse(savedRequest);
    }


    // === Private Helper ===

    // Tìm request và kiểm tra xem manager có quyền duyệt không
    private ExpenseRequest findRequestAndCheckManager(Long requestId, User manager) {
        ExpenseRequest request = expenseRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy request với ID: " + requestId));

        // Kiểm tra xem manager có phải là manager của employee tạo request không
        if (request.getRequester().getManager() == null ||
                request.getRequester().getManager().getId() != manager.getId()) {
            throw new AccessDeniedException("Bạn không phải là manager của người tạo request này");
        }
        return request;
    }

 
public List<ProjectResponse> getMyProjects(User manager) {
    return projectRepository.findByManager_Id(manager.getId())
            .stream()
            .map(projectMapper::toResponse)
            .collect(Collectors.toList());
}


public List<ExpenseRequestResponse> getManagerHistory(User manager) {
    return expenseRepository.findHistoryByManager(manager.getId())
            .stream()
            .map(expenseMapper::toResponse)
            .collect(Collectors.toList());
}
}