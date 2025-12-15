package com.example.secretweapon.service;

import com.example.secretweapon.exception.AccessDeniedException;
import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.ExpenseMapper;
import com.example.secretweapon.model.dto.RuleDecision;
import com.example.secretweapon.model.entity.*;
import com.example.secretweapon.model.enums.*;
import com.example.secretweapon.payload.request.ExpenseItemRequest;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRequestRepository expenseRepository;
    @Mock
    private ExpenseMapper expenseMapper;
    @Mock
    private RequestHistoryService historyService;
    @Mock
    private RuleService ruleService;
    @Mock
    private ProjectService projectService;
    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @InjectMocks
    private ExpenseService expenseService;

    // --- CREATE ---
    @Test
    @DisplayName("createExpense_NormalFlow_shouldCreateDraft")
    void createExpense_NormalFlow_shouldCreateDraft() {
        // Arrange
        User employee = new User();
        employee.setId(1L);
        Role role = new Role();
        role.setName(RoleName.ROLE_EMPLOYEE);
        employee.setRole(role);

        ExpenseRequestCreate requestDto = new ExpenseRequestCreate();
        requestDto.setProjectId(100L);
        requestDto.setTitle("Test Expense");
        requestDto.setAmount(new BigDecimal("100.00"));

        Project project = new Project();
        project.setId(100L);

        // Mock dependencies
        when(projectService.getProjectById(100L)).thenReturn(project);
        when(ruleService.getMatchingRules(any(), any(), any())).thenReturn(Collections.emptyList()); // No rules -> Allow
        when(anomalyDetectionService.isReceiptDuplicate(any())).thenReturn(false);
        
        // Mock save
        when(expenseRepository.save(any(ExpenseRequest.class))).thenAnswer(invocation -> {
            ExpenseRequest saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        ExpenseRequestResponse response = expenseService.createExpense(requestDto, employee);

        // Assert
        assertNotNull(response);
        verify(expenseRepository).save(argThat(r -> 
            r.getStatus() == ExpenseStatus.DRAFT && 
            r.getAmountTotal().equals(BigDecimal.ZERO) // Vì item list null/empty trong test này
        ));
    }

    // --- UPDATE ---
    @Test
    @DisplayName("updateExpense_StatusDraft_shouldUpdateSuccess")
    void updateExpense_StatusDraft_shouldUpdateSuccess() {
        // Arrange
        Long reqId = 1L;
        User employee = new User();
        employee.setId(1L);

        ExpenseRequest existingRequest = new ExpenseRequest();
        existingRequest.setId(reqId);
        existingRequest.setStatus(ExpenseStatus.DRAFT);
        existingRequest.setRequester(employee);
        existingRequest.setExpenseItems(new ArrayList<>()); // Init list để add

        ExpenseRequestCreate updateDto = new ExpenseRequestCreate();
        updateDto.setTitle("Updated Title");
        updateDto.setAmount(new BigDecimal("200.00"));
        // Mock item
        ExpenseItemRequest itemReq = new ExpenseItemRequest();
        itemReq.setAmount(new BigDecimal("200.00"));
        updateDto.setItems(List.of(itemReq));

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(existingRequest));
        when(expenseMapper.toItemEntity(any())).thenReturn(new ExpenseItem(null, null, ExpenseType.MEALS, new BigDecimal("200.00"), "Desc", null, null));
        when(expenseRepository.save(any(ExpenseRequest.class))).thenReturn(existingRequest);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        expenseService.updateExpense(reqId, updateDto, employee);

        // Assert
        assertEquals("Updated Title", existingRequest.getTitle());
        assertEquals(new BigDecimal("200.00"), existingRequest.getAmountTotal());
        verify(expenseRepository).save(existingRequest);
    }

    @Test
    @DisplayName("updateExpense_NotOwner_shouldThrowAccessDenied")
    void updateExpense_NotOwner_shouldThrowAccessDenied() {
        // Arrange
        Long reqId = 1L;
        User employee = new User();
        employee.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L); // Different ID

        ExpenseRequest existingRequest = new ExpenseRequest();
        existingRequest.setId(reqId);
        existingRequest.setRequester(otherUser);

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(existingRequest));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> 
            expenseService.updateExpense(reqId, new ExpenseRequestCreate(), employee)
        );
    }

    @Test
    @DisplayName("updateExpense_NotDraft_shouldThrowBadRequest")
    void updateExpense_NotDraft_shouldThrowBadRequest() {
        // Arrange
        Long reqId = 1L;
        User employee = new User();
        employee.setId(1L);

        ExpenseRequest existingRequest = new ExpenseRequest();
        existingRequest.setId(reqId);
        existingRequest.setRequester(employee);
        existingRequest.setStatus(ExpenseStatus.PENDING_MANAGER); // Not DRAFT

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(existingRequest));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> 
            expenseService.updateExpense(reqId, new ExpenseRequestCreate(), employee)
        );
    }

    // --- SUBMIT ---
    @Test
    @DisplayName("submitExpense_StatusDraft_shouldChangeToPendingManager")
    void submitExpense_StatusDraft_shouldChangeToPendingManager() {
        // Arrange
        Long reqId = 1L;
        User employee = new User();
        employee.setId(1L);

        ExpenseRequest request = new ExpenseRequest();
        request.setId(reqId);
        request.setRequester(employee);
        request.setStatus(ExpenseStatus.DRAFT);

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(request));
        when(expenseRepository.save(any())).thenReturn(request);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        expenseService.submitExpense(reqId, employee);

        // Assert
        assertEquals(ExpenseStatus.PENDING_MANAGER, request.getStatus());
        verify(expenseRepository).save(request);
    }

    // --- DELETE ---
    @Test
    @DisplayName("deleteExpense_StatusDraft_shouldDelete")
    void deleteExpense_StatusDraft_shouldDelete() {
        // Arrange
        Long reqId = 1L;
        User employee = new User();
        employee.setId(1L);

        ExpenseRequest request = new ExpenseRequest();
        request.setId(reqId);
        request.setRequester(employee);
        request.setStatus(ExpenseStatus.DRAFT);

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(request));

        // Act
        expenseService.deleteExpense(reqId, employee);

        // Assert
        verify(expenseRepository).delete(request);
    }

    // --- GET BY ID ---
    @Test
    @DisplayName("getExpenseById_ManagerOfRequester_shouldReturnRequest")
    void getExpenseById_ManagerOfRequester_shouldReturnRequest() {
        // Arrange
        User manager = new User();
        manager.setId(2L);
        // Set authority for manager
        // Note: In real SecurityContext, authorities are strings. In test, we mock behavior usually.
        // But your service checks: currentUser.getAuthorities().stream()...
        // So we need to ensure the passed 'currentUser' in test has authorities or mock the logic.
        // Looking at source: `boolean isManager = currentUser.getAuthorities()...`
        // We need to set authorities for the user object or use a spy.
        // Let's assume User implements UserDetails and returns authorities based on Role.
        Role roleManager = new Role();
        roleManager.setName(RoleName.ROLE_MANAGER);
        manager.setRole(roleManager); 
        
        User employee = new User();
        employee.setId(1L);
        employee.setManager(manager);

        ExpenseRequest request = new ExpenseRequest();
        request.setId(10L);
        request.setRequester(employee);

        when(expenseRepository.findById(10L)).thenReturn(Optional.of(request));
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        // Act
        ExpenseRequestResponse res = expenseService.getExpenseById(10L, manager);

        // Assert
        assertNotNull(res);
    }
}