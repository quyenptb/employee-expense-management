package com.example.secretweapon.controller;


import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.payload.response.ProjectResponse;
import com.example.secretweapon.service.ManagerService;
import com.example.secretweapon.service.SlackNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ManagerController {

    
    private final ManagerService managerService;
    private final SlackNotificationService slackNotificationService;


    // API Lấy danh sách request chờ duyệt (EPIC 03)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ExpenseRequestResponse>> getTeamPendingRequests(@AuthenticationPrincipal User manager) {
        List<ExpenseRequestResponse> requests = managerService.getTeamPendingRequests(manager);
        return ResponseEntity.ok(requests);
    }

    // API Duyệt request (EPIC 03)
    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<ExpenseRequestResponse> approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalRequest approvalRequest, // Comment có thể null khi approve
            @AuthenticationPrincipal User manager) {

        // Đảm bảo không null
        ApprovalRequest request = (approvalRequest == null) ? new ApprovalRequest() : approvalRequest;

        ExpenseRequestResponse approvedRequest = managerService.approveRequest(id, manager, request);
        if (approvedRequest != null)        {
            slackNotificationService.sendNotification("The expense request is " + approvalRequest.getDecision() + "by manager " + manager.getFullName() +  "because " + approvalRequest.getComment());
        }
        return ResponseEntity.ok(approvedRequest);
    }

    // API Từ chối request (EPIC 03)
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<ExpenseRequestResponse> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest approvalRequest, //Comment is compulsory when rejecting
            @AuthenticationPrincipal User manager) {

        ExpenseRequestResponse rejectedRequest = managerService.rejectRequest(id, manager, approvalRequest);
        if (rejectedRequest != null)        {
            slackNotificationService.sendNotification("The expense request is " + approvalRequest.getDecision() + "by manager " + manager.getFullName() +  "because " + approvalRequest.getComment());
        }
        return ResponseEntity.ok(rejectedRequest);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(managerService.getMyProjects(manager));
    }

    @GetMapping("/requests/history")
    public ResponseEntity<List<ExpenseRequestResponse>> getManagerHistory(@AuthenticationPrincipal User manager) {
            return ResponseEntity.ok(managerService.getManagerHistory(manager));
        }
}
