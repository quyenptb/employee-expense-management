package com.example.secretweapon;

import com.example.secretweapon.service.AnomalyDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AnomalyDetectionServiceImplTest {

    private AnomalyDetectionServiceImpl anomalyDetectionService;

    @BeforeEach
    void setUp() {
        anomalyDetectionService = new AnomalyDetectionServiceImpl();
    }

    @ParameterizedTest(name = "Role {0} with amount {1} should return {2}")
    @CsvSource({
            "EMPLOYEE, 49_000_000, true",
            "EMPLOYEE, 51_000_000, false",
            "MANAGER, 99_000_000, true",
            "MANAGER, 101_000_000, false"
    })
    void isAverageAmountValid_shouldReturnExpected(String roleName, BigDecimal amount, boolean expected) {
        boolean result = anomalyDetectionService.isAverageAmountValid(amount, roleName);
        assertEquals(expected, result);
    }

    @ParameterizedTest(name = "Role {0} with {1} requests should return {2}")
    @CsvSource({
            "EMPLOYEE, 9, true",
            "EMPLOYEE, 11, false",
            "MANAGER, 19, true",
            "MANAGER, 21, false"
    })
    void isRequestCountValid_shouldReturnExpected(String roleName, int requestCount, boolean expected) {
        boolean result = anomalyDetectionService.isRequestCountValid(requestCount, roleName);
        assertEquals(expected, result);
    }

    @ParameterizedTest(name = "Role {0} with amount {1} and {2} requests should be anomaly={3}")
    @CsvSource({
            "EMPLOYEE, 49_000_000, 9, false",
            "EMPLOYEE, 51_000_000, 9, true",
            "MANAGER, 101_000_000, 10, true",
            "MANAGER, 99_000_000, 21, true",
            "MANAGER, 99_000_000, 19, false"
    })
    void isUserAnomaly_shouldReturnExpected(String roleName, BigDecimal amount, int requestCount, boolean expected) {
        boolean result = anomalyDetectionService.isUserAnomaly(amount, requestCount, roleName);
        assertEquals(expected, result);
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




}
