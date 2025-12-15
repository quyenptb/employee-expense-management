package com.example.secretweapon.controller;


import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.service.FinanceService;
import com.example.secretweapon.service.SlackNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class FinanceController {

    
    private final FinanceService financeService;
    private final SlackNotificationService slackNotificationService;

    // API Lấy danh sách chờ Finance duyệt (EPIC 04)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ExpenseRequestResponse>> getPendingFinanceRequests() {
        List<ExpenseRequestResponse> requests = financeService.getPendingFinanceRequests();
        return ResponseEntity.ok(requests);
    }

    // API Finance duyệt chi (Final Approve) (EPIC 04)
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<ExpenseRequestResponse> approveFinanceRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequest approvalRequest,
            @AuthenticationPrincipal User financeUser) {

        ApprovalRequest request = (approvalRequest == null) ? new ApprovalRequest() : approvalRequest;
        ExpenseRequestResponse approvedRequest = financeService.approveFinanceRequest(id, financeUser, request);
        if (approvedRequest != null)        {
            slackNotificationService.sendNotification("The expense request is " + approvalRequest.getDecision() + "by finance " + financeUser.getFullName() +  "because " + approvalRequest.getComment());
        }
        return ResponseEntity.ok(approvedRequest);
    }

    // API Finance từ chối chi (EPIC 04)
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<ExpenseRequestResponse> rejectFinanceRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest approvalRequest,
            @AuthenticationPrincipal User financeUser) {

        ExpenseRequestResponse rejectedRequest = financeService.rejectFinanceRequest(id, financeUser, approvalRequest);
        if (rejectedRequest != null)        {
            slackNotificationService.sendNotification("The expense request is " + approvalRequest.getDecision() + "by finance " + financeUser.getFullName() +  "because " + approvalRequest.getComment());
        }
        return ResponseEntity.ok(rejectedRequest);
    }

    // API Finance đánh dấu đã thanh toán (EPIC 04)
    @PostMapping("/requests/{id}/pay")
    public ResponseEntity<ExpenseRequestResponse> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal User financeUser) {

        ExpenseRequestResponse paidRequest = financeService.markAsPaid(id, financeUser);
        if (paidRequest != null)        {
            slackNotificationService.sendNotification("The expense request is PAID by finance " + financeUser.getFullName());
        }
        return ResponseEntity.ok(paidRequest);
    }

    @GetMapping("/stats/department-spending")
public ResponseEntity<List<FinanceService.DepartmentSpendingDto>> getDepartmentSpending() {
    return ResponseEntity.ok(financeService.getSpendingByDepartment());
}

@GetMapping("/export")
public ResponseEntity<byte[]> exportExpenses() {
    byte[] csvData = financeService.exportExpenseToCsv();
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "expenses_report.csv");

    return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
}
}