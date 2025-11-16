package com.example.secretweapon.controller;


import com.example.secretweapon.model.dto.ApprovalRequest;
import com.example.secretweapon.model.dto.ExpenseRequestDto;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:3000")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    // API Lấy danh sách request chờ duyệt (EPIC 03)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ExpenseRequestDto>> getTeamPendingRequests(@AuthenticationPrincipal User manager) {
        List<ExpenseRequestDto> requests = managerService.getTeamPendingRequests(manager);
        return ResponseEntity.ok(requests);
    }

    // API Duyệt request (EPIC 03)
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<ExpenseRequestDto> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequest approvalRequest, // Comment có thể null khi approve
            @AuthenticationPrincipal User manager) {

        // Đảm bảo không null
        ApprovalRequest request = (approvalRequest == null) ? new ApprovalRequest() : approvalRequest;

        ExpenseRequestDto approvedRequest = managerService.approveRequest(id, manager, request);
        return ResponseEntity.ok(approvedRequest);
    }

    // API Từ chối request (EPIC 03)
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<ExpenseRequestDto> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest approvalRequest, // Comment là bắt buộc khi reject
            @AuthenticationPrincipal User manager) {

        ExpenseRequestDto rejectedRequest = managerService.rejectRequest(id, manager, approvalRequest);
        return ResponseEntity.ok(rejectedRequest);
    }
}
