package com.example.secretweapon.controller;


import com.example.secretweapon.model.dto.ApprovalRequest;
import com.example.secretweapon.model.dto.ExpenseRequestDto;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    // API Lấy danh sách chờ Finance duyệt (EPIC 04)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ExpenseRequestDto>> getPendingFinanceRequests() {
        List<ExpenseRequestDto> requests = financeService.getPendingFinanceRequests();
        return ResponseEntity.ok(requests);
    }

    // API Finance duyệt chi (Final Approve) (EPIC 04)
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<ExpenseRequestDto> approveFinanceRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequest approvalRequest,
            @AuthenticationPrincipal User financeUser) {

        ApprovalRequest request = (approvalRequest == null) ? new ApprovalRequest() : approvalRequest;
        ExpenseRequestDto approvedRequest = financeService.approveFinanceRequest(id, financeUser, request);
        return ResponseEntity.ok(approvedRequest);
    }

    // API Finance từ chối chi (EPIC 04)
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<ExpenseRequestDto> rejectFinanceRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest approvalRequest,
            @AuthenticationPrincipal User financeUser) {

        ExpenseRequestDto rejectedRequest = financeService.rejectFinanceRequest(id, financeUser, approvalRequest);
        return ResponseEntity.ok(rejectedRequest);
    }

    // API Finance đánh dấu đã thanh toán (EPIC 04)
    @PostMapping("/requests/{id}/pay")
    public ResponseEntity<ExpenseRequestDto> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal User financeUser) {

        ExpenseRequestDto paidRequest = financeService.markAsPaid(id, financeUser);
        return ResponseEntity.ok(paidRequest);
    }
}