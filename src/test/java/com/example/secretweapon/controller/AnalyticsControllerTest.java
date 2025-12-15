package com.example.secretweapon.controller;

import com.example.secretweapon.model.dto.ProjectHealthDTO;
import com.example.secretweapon.service.ProjectAnalyticsService;
import com.example.secretweapon.service.ProjectAnalyticsService.ProjectAnalyticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectAnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController).build();
    }

    @Test
    @DisplayName("getProjectForecast_ValidId_shouldReturnForecastData")
    void getProjectForecast_ValidId_shouldReturnForecastData() throws Exception {
        // Arrange
        Long projectId = 1L;
        ProjectAnalyticsDTO mockDto = ProjectAnalyticsDTO.builder()
                .projectId(projectId)
                .projectName("Test Project")
                .trendMessage("Spending is stable")
                .chartData(Collections.emptyList())
                .build();

        when(analyticsService.getProjectForecast(projectId)).thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/project/{id}/forecast", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Test Project"))
                .andExpect(jsonPath("$.trendMessage").value("Spending is stable"));
    }

    @Test
    @DisplayName("getProjectHealth_ValidId_shouldReturnHealthData")
    void getProjectHealth_ValidId_shouldReturnHealthData() throws Exception {
        // Arrange
        Long projectId = 1L;
        ProjectHealthDTO mockHealth = ProjectHealthDTO.builder()
                .projectId(projectId)
                .healthStatus("HEALTHY")
                .percentageUsed(50.0)
                .budgetTotal(new BigDecimal("1000"))
                .build();

        when(analyticsService.getProjectHealth(projectId)).thenReturn(mockHealth);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/project/{id}/health", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.percentageUsed").value(50.0));
    }
}