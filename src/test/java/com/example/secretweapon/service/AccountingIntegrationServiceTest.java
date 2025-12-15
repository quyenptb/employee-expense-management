package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountingIntegrationServiceTest {

    @Mock
    private QuickBooksService quickBooksService;

    @InjectMocks
    private AccountingIntegrationService accountingIntegrationService;

    @Test
    @DisplayName("syncToAccountingSystem_Success_shouldCallQuickBooksService")
    void syncToAccountingSystem_Success_shouldCallQuickBooksService() {
        // Arrange
        ExpenseRequest request = new ExpenseRequest();
        request.setRequestNo("EXP-001");
        User financeUser = new User();

        doNothing().when(quickBooksService).syncExpenseToQuickBooks(request);

        // Act
        accountingIntegrationService.syncToAccountingSystem(request, financeUser);

        // Assert
        verify(quickBooksService, times(1)).syncExpenseToQuickBooks(request);
    }

    @Test
    @DisplayName("syncToAccountingSystem_Exception_shouldCatchAndLog")
    void syncToAccountingSystem_Exception_shouldCatchAndLog() {
        // Arrange
        ExpenseRequest request = new ExpenseRequest();
        request.setRequestNo("EXP-FAIL");
        User financeUser = new User();

        // Giả lập QB bị lỗi
        doThrow(new RuntimeException("Connection timeout")).when(quickBooksService).syncExpenseToQuickBooks(request);

        // Act
        // Hàm này có try-catch nên sẽ không ném Exception ra ngoài
        accountingIntegrationService.syncToAccountingSystem(request, financeUser);

        // Assert
        verify(quickBooksService, times(1)).syncExpenseToQuickBooks(request);
        // Nếu code chạy đến đây mà không crash test tức là try-catch hoạt động tốt
    }
}