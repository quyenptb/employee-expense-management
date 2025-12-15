package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.ProjectHealthDTO;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAnalyticsServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ExpenseRequestRepository expenseRequestRepository;

    @InjectMocks
    private ProjectAnalyticsService analyticsService;

    @Test
    @DisplayName("getProjectForecast_NotEnoughData_shouldReturnMessage")
    void getProjectForecast_NotEnoughData_shouldReturnMessage() {
        // Arrange
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setName("New Project");
        project.setBudgetTotal(new BigDecimal("1000"));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(expenseRequestRepository.searchRequests(null, projectId, null, null, null))
                .thenReturn(Collections.emptyList());

        // Act
        ProjectAnalyticsService.ProjectAnalyticsDTO result = analyticsService.getProjectForecast(projectId);

        // Assert
        assertEquals("Not enough data to forecast", result.getTrendMessage());
        assertTrue(result.getChartData().isEmpty());
    }

    @Test
    @DisplayName("getProjectForecast_WithData_shouldCalculateTrend")
    void getProjectForecast_WithData_shouldCalculateTrend() {
        // Arrange
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setName("Active Project");
        project.setBudgetTotal(new BigDecimal("10000"));
        project.setStartDate(LocalDateTime.now().minusDays(10));

        // Create 2 expenses to form a line
        ExpenseRequest exp1 = new ExpenseRequest();
        exp1.setStatus(ExpenseStatus.APPROVED);
        exp1.setAmountTotal(new BigDecimal("100"));
        exp1.setCreatedAt(LocalDateTime.now().minusDays(5));

        ExpenseRequest exp2 = new ExpenseRequest();
        exp2.setStatus(ExpenseStatus.APPROVED);
        exp2.setAmountTotal(new BigDecimal("200"));
        exp2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(expenseRequestRepository.searchRequests(null, projectId, null, null, null))
                .thenReturn(List.of(exp1, exp2));

        // Act
        ProjectAnalyticsService.ProjectAnalyticsDTO result = analyticsService.getProjectForecast(projectId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getChartData().isEmpty());
        // Verify we have logic calculating points
        assertTrue(result.getChartData().size() >= 2);
    }

    @Test
    @DisplayName("getProjectHealth_CriticalStatus_shouldReturnCritical")
    void getProjectHealth_CriticalStatus_shouldReturnCritical() {
        // Arrange
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setName("Critical Project");
        project.setBudgetTotal(new BigDecimal("1000"));
        project.setBudgetUsed(new BigDecimal("950")); // 95% used
        project.setStartDate(LocalDateTime.now().minusDays(10));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        ProjectHealthDTO result = analyticsService.getProjectHealth(projectId);

        // Assert
        assertEquals("CRITICAL", result.getHealthStatus());
        assertEquals(95.0, result.getPercentageUsed());
    }

    @Test
    @DisplayName("getProjectHealth_HealthyStatus_shouldReturnHealthy")
    void getProjectHealth_HealthyStatus_shouldReturnHealthy() {
        // Arrange
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setBudgetTotal(new BigDecimal("1000"));
        project.setBudgetUsed(new BigDecimal("100")); // 10% used
        project.setStartDate(LocalDateTime.now().minusDays(10));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        ProjectHealthDTO result = analyticsService.getProjectHealth(projectId);

        // Assert
        assertEquals("HEALTHY", result.getHealthStatus());
    }
}