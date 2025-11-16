package com.example.secretweapon.service;



import com.example.secretweapon.model.dto.ApprovalRequest;
import com.example.secretweapon.model.dto.ExpenseRequestDto;
import com.example.secretweapon.exception.AccessDeniedException;
import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.RequestHistory;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.model.enums.HistoryAction;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagerService {

    @Autowired
    private ExpenseRequestRepository expenseRepository;

    // Lấy các request đang chờ manager duyệt (EPIC 03)
    public List<ExpenseRequestDto> getTeamPendingRequests(User manager) {
        return expenseRepository.findByStatusAndEmployee_ManagerOrderByCreatedAtAsc(
                ExpenseStatus.PENDING_MANAGER,
                manager
        ).stream().map(ExpenseRequestDto::new).collect(Collectors.toList());
    }

    // Manager duyệt request (EPIC 03)
    @Transactional
    public ExpenseRequestDto approveRequest(Long requestId, User manager, ApprovalRequest approvalRequest) {
        ExpenseRequest request = findRequestAndCheckManager(requestId, manager);

        if (request.getStatus() != ExpenseStatus.PENDING_MANAGER) {
            throw new BadRequestException("Request không ở trạng thái PENDING_MANAGER");
        }

        request.setStatus(ExpenseStatus.PENDING_FINANCE); // Chuyển cho Finance
        request.addHistory(new RequestHistory(
                request,
                manager,
                HistoryAction.MANAGER_APPROVED,
                approvalRequest.getComment()
        ));

        ExpenseRequest savedRequest = expenseRepository.save(request);
        return new ExpenseRequestDto(savedRequest);
    }

    // Manager từ chối request (EPIC 03)
    @Transactional
    public ExpenseRequestDto rejectRequest(Long requestId, User manager, ApprovalRequest approvalRequest) {
        if (approvalRequest.getComment() == null || approvalRequest.getComment().isBlank()) {
            throw new BadRequestException("Phải cung cấp lý do khi từ chối");
        }

        ExpenseRequest request = findRequestAndCheckManager(requestId, manager);

        if (request.getStatus() != ExpenseStatus.PENDING_MANAGER) {
            throw new BadRequestException("Request không ở trạng thái PENDING_MANAGER");
        }

        request.setStatus(ExpenseStatus.REJECTED_MANAGER); // Trả về
        request.addHistory(new RequestHistory(
                request,
                manager,
                HistoryAction.MANAGER_REJECTED,
                approvalRequest.getComment()
        ));

        ExpenseRequest savedRequest = expenseRepository.save(request);
        return new ExpenseRequestDto(savedRequest);
    }


    // === Private Helper ===

    // Tìm request và kiểm tra xem manager có quyền duyệt không
    private ExpenseRequest findRequestAndCheckManager(Long requestId, User manager) {
        ExpenseRequest request = expenseRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy request với ID: " + requestId));

        // Kiểm tra xem manager có phải là manager của employee tạo request không
        if (request.getEmployee().getManager() == null ||
                request.getEmployee().getManager().getId() != manager.getId()) {
            throw new AccessDeniedException("Bạn không phải là manager của người tạo request này");
        }
        return request;
    }
}