package com.example.secretweapon.service;


import com.example.secretweapon.model.dto.ApprovalRequest;
import com.example.secretweapon.model.dto.ExpenseRequestDto;
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
public class FinanceService {

    @Autowired
    private ExpenseRequestRepository expenseRepository;

    // Lấy các request đang chờ Finance duyệt (EPIC 04)
    public List<ExpenseRequestDto> getPendingFinanceRequests() {
        return expenseRepository.findByStatusInOrderByUpdatedAtDesc(
                List.of(ExpenseStatus.PENDING_FINANCE)
        ).stream().map(ExpenseRequestDto::new).collect(Collectors.toList());
    }

    // Finance duyệt (final approve) (EPIC 04)
    @Transactional
    public ExpenseRequestDto approveFinanceRequest(Long requestId, User financeUser, ApprovalRequest approvalRequest) {
        ExpenseRequest request = findRequestByIdAndCheckStatus(requestId, ExpenseStatus.PENDING_FINANCE);

        request.setStatus(ExpenseStatus.APPROVED); // Chờ thanh toán
        request.addHistory(new RequestHistory(
                request,
                financeUser,
                HistoryAction.FINANCE_APPROVED,
                approvalRequest.getComment()
        ));

        return new ExpenseRequestDto(expenseRepository.save(request));
    }

    // Finance từ chối (EPIC 04)
    @Transactional
    public ExpenseRequestDto rejectFinanceRequest(Long requestId, User financeUser, ApprovalRequest approvalRequest) {
        if (approvalRequest.getComment() == null || approvalRequest.getComment().isBlank()) {
            throw new BadRequestException("Phải cung cấp lý do khi từ chối");
        }

        ExpenseRequest request = findRequestByIdAndCheckStatus(requestId, ExpenseStatus.PENDING_FINANCE);

        request.setStatus(ExpenseStatus.REJECTED_FINANCE);
        request.addHistory(new RequestHistory(
                request,
                financeUser,
                HistoryAction.FINANCE_REJECTED,
                approvalRequest.getComment()
        ));

        return new ExpenseRequestDto(expenseRepository.save(request));
    }

    // Finance đánh dấu đã thanh toán (EPIC 04)
    @Transactional
    public ExpenseRequestDto markAsPaid(Long requestId, User financeUser) {
        ExpenseRequest request = findRequestByIdAndCheckStatus(requestId, ExpenseStatus.APPROVED);

        request.setStatus(ExpenseStatus.PAID); // Hoàn thành
        request.addHistory(new RequestHistory(
                request,
                financeUser,
                HistoryAction.MARKED_AS_PAID,
                "Đã xử lý thanh toán"
        ));

        return new ExpenseRequestDto(expenseRepository.save(request));
    }

    // === Private Helper ===

    private ExpenseRequest findRequestByIdAndCheckStatus(Long requestId, ExpenseStatus expectedStatus) {
        ExpenseRequest request = expenseRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy request với ID: " + requestId));

        if(request.getStatus() != expectedStatus) {
            throw new BadRequestException("Request không ở trạng thái " + expectedStatus);
        }
        return request;
    }
}