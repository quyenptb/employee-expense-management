package com.example.secretweapon.service.impl;

import com.example.secretweapon.service.AnomalyDetectionServiceImpl;
import com.example.secretweapon.service.ReceiptHasher;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceImplTest {

    @Mock
    private ExpenseRequestRepository expenseRequestRepository;

    @Mock
    private ReceiptHasher receiptHasher;

    @InjectMocks
    private AnomalyDetectionServiceImpl anomalyDetectionService;

    // --- 1. Test Threshold Logic (Giữ lại logic cũ nhưng viết gọn hơn) ---

    @ParameterizedTest(name = "Role {0} with amount {1} should return {2}")
    @CsvSource({
            "EMPLOYEE, 49000000, true",  // < 50M -> OK
            "EMPLOYEE, 51000000, false", // > 50M -> Anomaly
            "MANAGER, 99000000, true",   // < 100M -> OK
            "MANAGER, 101000000, false"  // > 100M -> Anomaly
    })
    void isAverageAmountValid_shouldReturnExpected(String roleName, BigDecimal amount, boolean expected) {
        assertEquals(expected, anomalyDetectionService.isAverageAmountValid(amount, roleName));
    }

    @ParameterizedTest(name = "Role {0} with {1} requests should return {2}")
    @CsvSource({
            "EMPLOYEE, 9, true",   // < 10 -> OK
            "EMPLOYEE, 11, false", // > 10 -> Anomaly
            "MANAGER, 19, true",   // < 20 -> OK
            "MANAGER, 21, false"   // > 20 -> Anomaly
    })
    void isRequestCountValid_shouldReturnExpected(String roleName, int count, boolean expected) {
        assertEquals(expected, anomalyDetectionService.isRequestCountValid(count, roleName));
    }

    // --- 2. Test Receipt Duplicate Logic (Async) ---

    @Test
    @DisplayName("isReceiptDuplicate_HasherReturnsAlert_shouldReturnTrue")
    void isReceiptDuplicate_HasherReturnsAlert_shouldReturnTrue() {
        // Arrange
        String imageUrl = "http://fake.url/img.jpg";
        // Giả lập Hasher trả về chuỗi bắt đầu bằng "ALERT:"
        when(receiptHasher.processReceiptAsync(anyString()))
                .thenReturn(CompletableFuture.completedFuture("ALERT: Duplicate found! Distance 0.1"));

        // Act
        boolean result = anomalyDetectionService.isReceiptDuplicate(imageUrl);

        // Assert
        assertTrue(result, "Should identify as duplicate when hasher alerts");
    }

    @Test
    @DisplayName("isReceiptDuplicate_HasherReturnsUnique_shouldReturnFalse")
    void isReceiptDuplicate_HasherReturnsUnique_shouldReturnFalse() {
        // Arrange
        String imageUrl = "http://fake.url/img.jpg";
        when(receiptHasher.processReceiptAsync(anyString()))
                .thenReturn(CompletableFuture.completedFuture("Image is unique. New hash saved."));

        // Act
        boolean result = anomalyDetectionService.isReceiptDuplicate(imageUrl);

        // Assert
        assertFalse(result, "Should be unique");
    }

    @Test
    @DisplayName("isReceiptDuplicate_NullUrl_shouldReturnFalse")
    void isReceiptDuplicate_NullUrl_shouldReturnFalse() {
        assertFalse(anomalyDetectionService.isReceiptDuplicate(null));
        assertFalse(anomalyDetectionService.isReceiptDuplicate(""));
    }
    
    @Test
    @DisplayName("isReceiptDuplicate_Exception_shouldReturnFalseAndLog")
    void isReceiptDuplicate_Exception_shouldReturnFalseAndLog() {
        // Arrange
        when(receiptHasher.processReceiptAsync(anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Async Error")));
        
        // Act
        boolean result = anomalyDetectionService.isReceiptDuplicate("http://error.url");
        
        // Assert
        // Khi xảy ra lỗi (InterruptedException/ExecutionException), service catch lại và trả về false (safe fail)
        assertFalse(result);
    }
}




    /*
    @Test
    @DisplayName("shouldReturnFalse_EmployeeHasAmountGreaterThanAverageAmount")
    void shouldReturnFalse_EmployeeHasAmountGreaterThanAverageAmount() {
        BigDecimal employeeAmount = BigDecimal.valueOf(51_000_000);
        String roleName = "EMPLOYEE";
        //LocalDate startDate = LocalDate.of(2025, 8, 11); //3 tháng trước
        boolean result = anomalyDetectionService.isAverageAmountValid(employeeAmount, roleName);

        assertFalse(result);
        //chưa check verify lưu vội?
    }

    @Test
    @DisplayName("shouldReturnTrue_EmployeeHasAmountEqualsOrLowerThanAverageAmount")
    void shouldReturnTrue_EmployeeHasAmountEqualsOrLowerThanAverageAmount() {
        BigDecimal employeeAmount = BigDecimal.valueOf(49_000_000);
        String roleName = "EMPLOYEE";
        //LocalDate startDate = LocalDate.of(2025, 8, 11); //3 tháng trước
        boolean result = anomalyDetectionService.isAverageAmountValid(employeeAmount, roleName);

        assertTrue(result);
        //chưa check verify lưu vội?
    }

    @Test
    @DisplayName("shouldReturnFalse_ManagerHasAmountGreaterThanAverageAmount")
    void shouldReturnFalse_ManagerHasAmountGreaterThanAverageAmount() {
        BigDecimal employeeAmount = BigDecimal.valueOf(101_000_000);
        String roleName = "MANAGER";
        //LocalDate startDate = LocalDate.of(2025, 8, 11); //3 tháng trước
        boolean result = anomalyDetectionService.isAverageAmountValid(employeeAmount, roleName);

        assertFalse(result);
        //chưa check verify lưu vội?
    }

    @Test
    @DisplayName("shouldReturnTrue_ManagerHasAmountEqualsOrLowerThanAverageAmount")
    void shouldReturnTrue_ManagerHasAmountEqualsOrLowerThanAverageAmount() {
        BigDecimal employeeAmount = BigDecimal.valueOf(99_000_000);
        String roleName = "MANAGER";
        //LocalDate startDate = LocalDate.of(2025, 8, 11); //3 tháng trước
        boolean result = anomalyDetectionService.isAverageAmountValid(employeeAmount, roleName);

        assertTrue(result);
        //chưa check verify lưu vội?
    }


    //------Request frequency-----------
    @Test
    @DisplayName("isRequestCountValid_EmployeeHasMoreRequestThanThreshold_shouldReturnFalse")
    void isRequestCountValid_EmployeeHasMoreRequestThanThreshold_shouldReturnFalse() {
        // Arrange
        int employeeRequestCount = 11;
        String roleName = "EMPLOYEE";
        // Act
        boolean result = anomalyDetectionService.isRequestCountValid(employeeRequestCount, roleName);
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isRequestCountValid_ManagerHasMoreRequestThanThreshold_shouldReturnFalse")
    void isRequestCountValid_ManagerHasMoreRequestThanThreshold_shouldReturnFalse() {
        // Arrange
        int managerRequestCount = 21;
        String roleName = "MANAGER";
        // Act
        boolean result = anomalyDetectionService.isRequestCountValid(managerRequestCount, roleName);
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isRequestCountValid_EmployeeHasRequestEqualsOrLowerThanThreshold_shouldReturnTrue")
    void isRequestCountValid_EmployeeHasRequestEqualsOrLowerThanThreshold_shouldReturnTrue() {
        // Arrange
        int employeeRequestCount = 9;
        String roleName = "EMPLOYEE";
        // Act
        boolean result = anomalyDetectionService.isRequestCountValid(employeeRequestCount, roleName);
        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isRequestCountValid_ManagerHasRequestEqualsOrLowerThanThreshold_shouldReturnTrue")
    void isRequestCountValid_ManagerHasRequestEqualsOrLowerThanThreshold_shouldReturnTrue() {
        // Arrange
        int managerRequestCount = 19;
        String roleName = "MANAGER";
        // Act
        boolean result = anomalyDetectionService.isRequestCountValid(managerRequestCount, roleName);
        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isUserAnomaly_Employee_shouldReturnFalse")
    void isUserAnomaly_Employee_shouldReturnFalse() {
        // Arrange
        int employeeRequestCount = 9;
        BigDecimal employeeAmount = BigDecimal.valueOf(49_000_000);
        String roleName = "EMPLOYEE";
        // Act
        boolean result = anomalyDetectionService.isUserAnomaly(employeeAmount, employeeRequestCount, roleName);
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isUserAnomaly_MANAGER_shouldReturnFalse")
    void isUserAnomaly_MANAGER_shouldReturnFalse() {
        // Arrange
        int managerRequestCount = 19;
        BigDecimal managerAmount = BigDecimal.valueOf(99_000_000);
        String roleName = "MANAGER";
        // Act
        boolean result = anomalyDetectionService.isUserAnomaly(managerAmount, managerRequestCount, roleName);
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isUserAnomaly_EmployeeAmountTooHigh_shouldReturnTrue")
    void isUserAnomaly_EmployeeAmountTooHigh_shouldReturnTrue() {
        BigDecimal employeeAmount = BigDecimal.valueOf(51_000_000);
        int employeeRequestCount = 9;
        String roleName = "EMPLOYEE";

        boolean result = anomalyDetectionService.isUserAnomaly(employeeAmount, employeeRequestCount, roleName);

        assertTrue(result);
    }

    @Test
    @DisplayName("isUserAnomaly_EmployeeRequestTooHigh_shouldReturnTrue")
    void isUserAnomaly_EmployeeRequestTooHigh_shouldReturnTrue() {
        BigDecimal employeeAmount = BigDecimal.valueOf(49_000_000);
        int employeeRequestCount = 11;
        String roleName = "EMPLOYEE";

        boolean result = anomalyDetectionService.isUserAnomaly(employeeAmount, employeeRequestCount, roleName);

        assertTrue(result);
    }

    @Test
    @DisplayName("isUserAnomaly_BothAmountAndRequestTooHigh_shouldReturnTrue")
    void isUserAnomaly_BothAmountAndRequestTooHigh_shouldReturnTrue() {
        BigDecimal employeeAmount = BigDecimal.valueOf(51_000_000);
        int employeeRequestCount = 11;
        String roleName = "EMPLOYEE";

        boolean result = anomalyDetectionService.isUserAnomaly(employeeAmount, employeeRequestCount, roleName);

        assertTrue(result);
    }

    */





