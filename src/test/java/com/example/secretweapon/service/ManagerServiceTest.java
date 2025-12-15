package com.example.secretweapon.service;

import com.example.secretweapon.mapper.ExpenseMapper;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.service.ManagerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ExpenseRequestRepository expenseRepository;
    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ManagerService managerService;

    @Test
    void approveRequest_Success() {
        Long reqId = 1L;
        User manager = new User();
        manager.setId(2L);

        User employee = new User();
        employee.setId(3L);
        employee.setManager(manager);

        ExpenseRequest request = new ExpenseRequest();
        request.setId(reqId);
        request.setRequester(employee);
        request.setStatus(ExpenseStatus.PENDING_MANAGER);

        ApprovalRequest approvalReq = new ApprovalRequest();
        approvalReq.setComment("Good job");

        when(expenseRepository.findById(reqId)).thenReturn(Optional.of(request));
        when(expenseRepository.save(any(ExpenseRequest.class))).thenReturn(request);
        when(expenseMapper.toResponse(any())).thenReturn(new ExpenseRequestResponse());

        managerService.approveRequest(reqId, manager, approvalReq);

        assertEquals(ExpenseStatus.PENDING_FINANCE, request.getStatus());
        verify(expenseRepository).save(request);
    }

    @Test
    void approveRequest_Fail_WrongManager() {
        User manager = new User();
        manager.setId(2L);

        User otherManager = new User();
        otherManager.setId(99L);

        User employee = new User();
        employee.setManager(otherManager);

        ExpenseRequest request = new ExpenseRequest();
        request.setRequester(employee);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class, () ->
                managerService.approveRequest(1L, manager, new ApprovalRequest())
        );
    }
}