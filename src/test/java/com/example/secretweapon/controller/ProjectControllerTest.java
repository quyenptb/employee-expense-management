package com.example.secretweapon.controller;

import com.example.secretweapon.payload.request.ProjectUpdateBudgetRequest;
import com.example.secretweapon.payload.response.ProjectResponse;
import com.example.secretweapon.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
    }

    @Test
    @DisplayName("getAllProjects_shouldReturnList")
    void getAllProjects_shouldReturnList() throws Exception {
        ProjectResponse p = ProjectResponse.builder().id(1L).name("Project Alpha").build();
        when(projectService.getAllProjects()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Project Alpha"));
    }

    @Test
    @DisplayName("updateProjectDetails_Valid_shouldReturnUpdated")
    void updateProjectDetails_Valid_shouldReturnUpdated() throws Exception {
        Long id = 1L;
        ProjectUpdateBudgetRequest req = new ProjectUpdateBudgetRequest();
        req.setBudgetTotal(new BigDecimal("5000"));
        
        ProjectResponse res = ProjectResponse.builder().id(id).budgetTotal(new BigDecimal("5000")).build();

        when(projectService.updateProjectDetails(eq(id), any(ProjectUpdateBudgetRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/projects/{id}/details", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetTotal").value(5000));
    }
}