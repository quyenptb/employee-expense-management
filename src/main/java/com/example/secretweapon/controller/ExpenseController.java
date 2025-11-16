package com.example.secretweapon.controller;


import com.example.secretweapon.model.dto.CreateExpenseRequest;
import com.example.secretweapon.model.dto.ExpenseRequestDto;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:3000")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    // API Tạo request mới (EPIC 02)
    @PostMapping
    public ResponseEntity<ExpenseRequestDto> createExpense(
            @Valid @RequestBody CreateExpenseRequest createRequest,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestDto newExpense = expenseService.createExpense(createRequest, currentUser);
        return new ResponseEntity<>(newExpense, HttpStatus.CREATED);
    }

    // API Lấy danh sách request của tôi (EPIC 05)
    @GetMapping("/my")
    public ResponseEntity<List<ExpenseRequestDto>> getMyExpenses(@AuthenticationPrincipal User currentUser) {
        List<ExpenseRequestDto> expenses = expenseService.getMyExpenses(currentUser);
        return ResponseEntity.ok(expenses);
    }

    // API Lấy chi tiết 1 request (EPIC 05)
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseRequestDto> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestDto expense = expenseService.getExpenseById(id, currentUser);
        return ResponseEntity.ok(expense);
    }

    // API Cập nhật request (khi là DRAFT) (EPIC 02)
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseRequestDto> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody CreateExpenseRequest updateRequest,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestDto updatedExpense = expenseService.updateExpense(id, updateRequest, currentUser);
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
    public ResponseEntity<ExpenseRequestDto> submitExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        ExpenseRequestDto submittedExpense = expenseService.submitExpense(id, currentUser);
        return ResponseEntity.ok(submittedExpense);
    }
}