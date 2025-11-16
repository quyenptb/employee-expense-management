package com.example.secretweapon.service;



import com.example.secretweapon.model.dto.CreateExpenseRequest;
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
public class ExpenseService {

    @Autowired
    private ExpenseRequestRepository expenseRepository;

    @Autowired
    private RequestHistoryService historyService;

    // Lấy request theo ID (cho tất cả) (EPIC 05)
    @Transactional(readOnly = true)
    public ExpenseRequestDto getExpenseById(Long id, User currentUser) {
        ExpenseRequest request = findRequestById(id);

        // Chỉ chủ sở hữu, manager của chủ sở hữu, hoặc Finance/Admin mới được xem
        boolean isOwner = request.getEmployee().getId() == currentUser.getId();
        boolean isManager = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) &&
                request.getEmployee().getManager() != null &&
                request.getEmployee().getManager().getId() == currentUser.getId();
        boolean isFinanceOrAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FINANCE") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isManager && !isFinanceOrAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xem request này");
        }

        return new ExpenseRequestDto(request);
    }

    // Employee tạo request mới (EPIC 02)
    @Transactional
    public ExpenseRequestDto createExpense(CreateExpenseRequest dto, User employee) {
        ExpenseRequest request = new ExpenseRequest();
        request.setEmployee(employee);
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setAmount(dto.getAmount());
        request.setReceiptImageUrl(dto.getReceiptImageUrl());
        request.setStatus(ExpenseStatus.DRAFT); // Trạng thái ban đầu

        // Ghi log (dùng helper method trong entity)
        request.addHistory(new RequestHistory(request, employee, HistoryAction.CREATED, "Tạo nháp"));

        ExpenseRequest savedRequest = expenseRepository.save(request);
        return new ExpenseRequestDto(savedRequest);
    }

    // Employee cập nhật request (khi là DRAFT) (EPIC 02)
    @Transactional
    public ExpenseRequestDto updateExpense(Long id, CreateExpenseRequest dto, User employee) {
        ExpenseRequest request = findRequestAndCheckOwnership(id, employee);

        if (request.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Chỉ có thể cập nhật request ở trạng thái DRAFT");
        }

        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setAmount(dto.getAmount());
        request.setReceiptImageUrl(dto.getReceiptImageUrl());

        ExpenseRequest updatedRequest = expenseRepository.save(request);
        return new ExpenseRequestDto(updatedRequest);
    }

    // Employee xóa request (khi là DRAFT) (EPIC 02)
    @Transactional
    public void deleteExpense(Long id, User employee) {
        ExpenseRequest request = findRequestAndCheckOwnership(id, employee);

        if (request.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Chỉ có thể xóa request ở trạng thái DRAFT");
        }

        expenseRepository.delete(request);
    }

    // Employee gửi request cho manager (EPIC 02)
    @Transactional
    public ExpenseRequestDto submitExpense(Long id, User employee) {
        ExpenseRequest request = findRequestAndCheckOwnership(id, employee);

        if (request.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Request đã được gửi hoặc đã xử lý");
        }

        request.setStatus(ExpenseStatus.PENDING_MANAGER);
        request.addHistory(new RequestHistory(request, employee, HistoryAction.SUBMITTED, "Gửi duyệt cho Manager"));

        ExpenseRequest savedRequest = expenseRepository.save(request);
        return new ExpenseRequestDto(savedRequest);
    }

    // Employee xem các request của mình (EPIC 05)
    @Transactional(readOnly = true)
    public List<ExpenseRequestDto> getMyExpenses(User employee) {
        return expenseRepository.findByEmployeeOrderByCreatedAtDesc(employee).stream()
                .map(ExpenseRequestDto::new)
                .collect(Collectors.toList());
    }


    private ExpenseRequest findRequestById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy request với ID: " + id));
    }

    // Tìm request và kiểm tra xem có phải của user đó không
    private ExpenseRequest findRequestAndCheckOwnership(Long id, User employee) {
        ExpenseRequest request = findRequestById(id);
        if (request.getEmployee().getId() != employee.getId()) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa request này");
        }
        return request;
    }
}