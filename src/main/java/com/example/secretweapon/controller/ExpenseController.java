package com.example.secretweapon.controller;


import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:3000")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // API Tạo request mới (EPIC 02)
    @PostMapping
    public ResponseEntity<ExpenseRequestResponse> createExpense(
            @Valid @RequestBody ExpenseRequestCreate createRequest,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestResponse newExpense = expenseService.createExpense(createRequest, currentUser);
        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    // API Lấy danh sách request của tôi (EPIC 05)
    @GetMapping("/my")
    public ResponseEntity<List<ExpenseRequestResponse>> getMyExpenses(@AuthenticationPrincipal User currentUser) {
        List<ExpenseRequestResponse> expenses = expenseService.getMyExpenses(currentUser);
        return ResponseEntity.ok(expenses);
    }

    // API Lấy chi tiết 1 request (EPIC 05)
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseRequestResponse> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestResponse expense = expenseService.getExpenseById(id, currentUser);
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExpenseRequestResponse>> getAllExpenseRequest(@AuthenticationPrincipal User adminUser) {
        List<ExpenseRequestResponse> listExpenseRequestResponses = expenseService.getAllExpenseRequest(adminUser);
        return  ResponseEntity.ok(listExpenseRequestResponses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ExpenseRequestResponse>> searchRequests(
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long requesterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal User currentUser) {
        
        List<ExpenseRequestResponse> responses = expenseService.searchAllRequests(currentUser, status, projectId, requesterId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    // API Cập nhật request (khi là DRAFT) (EPIC 02)
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseRequestResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestCreate updateRequest,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestResponse updatedExpense = expenseService.updateExpense(id, updateRequest, currentUser);
        return ResponseEntity.ok(updatedExpense);
    }

    // API Xóa request (khi là DRAFT) (EPIC 02)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        expenseService.deleteExpense(id, currentUser);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // API Gửi duyệt (Submit) (EPIC 02)
    @PostMapping("/{id}/submit")
    public ResponseEntity<ExpenseRequestResponse> submitExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestResponse submittedExpense = expenseService.submitExpense(id, currentUser);
        return ResponseEntity.ok(submittedExpense);
    }
}