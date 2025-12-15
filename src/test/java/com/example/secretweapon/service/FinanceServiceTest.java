package com.example.secretweapon.service;

import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.model.entity.*;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.model.enums.HistoryAction;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.repository.ProjectBudgetHistoryRepository;
import com.example.secretweapon.service.AccountingIntegrationService;
import com.example.secretweapon.service.FinanceService;
import com.example.secretweapon.service.ProjectService;
import com.example.secretweapon.mapper.ExpenseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private ExpenseRequestRepository expenseRepository;
    @Mock
    private ExpenseMapper expenseMapper;
    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectBudgetHistoryRepository projectBudgetHistoryRepository;
    @Mock
    private AccountingIntegrationService accountingIntegrationService;

    @InjectMocks
    private FinanceService financeService;

    @Test
    @DisplayName("approveFinanceRequest_ValidRequest_shouldReturnApprovedResponse")
    void approveFinanceRequest_ValidRequest_shouldReturnApprovedResponse() {
        // Arrange
        Long reqId = 1L;
        User financeUser = new User();
        financeUser.setId(2L);

        Project project = new Project();
        project.setBudgetTotal(new BigDecimal("10000000"));
        project.setBudgetUsed(new BigDecimal("2000000"));

        ExpenseRequest request = new ExpenseRequest();
        request.setId(reqId);
        request.setStatus(ExpenseStatus.PENDING_FINANCE);
        request.setAmountTotal(new BigDecimal("500000"));
        request.setProject(project);

        ApprovalRequest approvalReq = new ApprovalRequest();
        approvalReq.setComment("Approved");

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(request));
        when(expenseRepository.save(any(ExpenseRequest.class))).thenReturn(request);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        financeService.approveFinanceRequest(reqId, financeUser, approvalReq);

        // Assert
        assertEquals(ExpenseStatus.APPROVED, request.getStatus());
        assertEquals(new BigDecimal("9500000"), project.getBudgetTotal());
        assertEquals(new BigDecimal("2500000"), project.getBudgetUsed());
        
        verify(projectBudgetHistoryRepository).save(any(ProjectBudgetHistory.class));
        verify(expenseRepository).save(request);
    }

    @Test
    @DisplayName("rejectFinanceRequest_ValidRequest_shouldReturnRejectedResponse")
    void rejectFinanceRequest_ValidRequest_shouldReturnRejectedResponse() {
        // Arrange
        Long reqId = 1L;
        User financeUser = new User();
        ApprovalRequest approvalReq = new ApprovalRequest();
        approvalReq.setComment("Rejected due to policy");

        ExpenseRequest request = new ExpenseRequest();
        request.setId(reqId);
        request.setStatus(ExpenseStatus.PENDING_FINANCE);

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(request));
        when(expenseRepository.save(any(ExpenseRequest.class))).thenReturn(request);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        financeService.rejectFinanceRequest(reqId, financeUser, approvalReq);

        // Assert
        assertEquals(ExpenseStatus.FINANCE_REJECTED, request.getStatus());
        verify(expenseRepository).save(request);
    }

    @Test
    @DisplayName("rejectFinanceRequest_NoComment_shouldThrowBadRequestException")
    void rejectFinanceRequest_NoComment_shouldThrowBadRequestException() {
        // Arrange
        ApprovalRequest approvalReq = new ApprovalRequest();
        approvalReq.setComment(""); 
        
        // Act & Assert
        assertThrows(BadRequestException.class, () -> 
            financeService.rejectFinanceRequest(1L, new User(), approvalReq)
        );
    }

    @Test
    @DisplayName("markAsPaid_ValidRequest_shouldReturnPaidResponse")
    void markAsPaid_ValidRequest_shouldReturnPaidResponse() {
        // Arrange
        Long reqId = 1L;
        User financeUser = new User();
        ExpenseRequest request = new ExpenseRequest();
        request.setId(reqId);
        request.setStatus(ExpenseStatus.APPROVED);

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(request));
        when(expenseRepository.save(any(ExpenseRequest.class))).thenReturn(request);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        financeService.markAsPaid(reqId, financeUser);

        // Assert
        assertEquals(ExpenseStatus.PAID, request.getStatus());
        verify(accountingIntegrationService).syncToAccountingSystem(request, financeUser);
        verify(expenseRepository).save(request);
    }
}