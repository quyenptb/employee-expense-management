package com.example.secretweapon.service;

import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.ProjectMapper;
import com.example.secretweapon.mapper.UserMapper;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.ProjectUpdateBudgetRequest;
import com.example.secretweapon.payload.response.ProjectResponse;
import com.example.secretweapon.payload.response.UserSummary;
import com.example.secretweapon.repository.ProjectRepository;
import com.example.secretweapon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("getProjectById_ExistingId_shouldReturnProject")
    void getProjectById_ExistingId_shouldReturnProject() {
        // Arrange
        Long projectId = 1L;
        Project project = new Project();
        project.setId(projectId);
        project.setName("Test Project");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        Project result = projectService.getProjectById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals("Test Project", result.getName());
    }

    @Test
    @DisplayName("getProjectById_NotFound_shouldThrowException")
    void getProjectById_NotFound_shouldThrowException() {
        // Arrange
        Long projectId = 99L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(projectId));
    }

    @Test
    @DisplayName("getAllProjects_shouldReturnList")
    void getAllProjects_shouldReturnList() {
        // Arrange
        Project project = new Project();
        project.setId(1L);
        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(projectMapper.toResponse(project)).thenReturn(new ProjectResponse());

        // Act
        List<ProjectResponse> results = projectService.getAllProjects();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("updateProjectDetails_ValidRequest_shouldUpdateAndReturnResponse")
    void updateProjectDetails_ValidRequest_shouldUpdateAndReturnResponse() {
        // Arrange
        Long projectId = 1L;
        ProjectUpdateBudgetRequest request = new ProjectUpdateBudgetRequest();
        request.setBudgetTotal(new BigDecimal("50000000"));
        request.setManagerId(2L);

        Project existingProject = new Project();
        existingProject.setId(projectId);
        existingProject.setBudgetTotal(new BigDecimal("10000000"));

        User manager = new User();
        manager.setId(2L);
        manager.setFullName("New Manager");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(projectRepository.save(any(Project.class))).thenReturn(existingProject);
        
        // Mock mapper manually since the service uses a private helper method that calls the mapper or builds response
        // Note: In the source code provided, mapToResponse is a private method in service using getters/builders.
        // If mapToResponse uses userMapper, we need to mock userMapper.
        when(userMapper.toSummary(manager)).thenReturn(new UserSummary(2L, "New Manager", null, null));

        // Act
        ProjectResponse response = projectService.updateProjectDetails(projectId, request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("50000000"), response.getBudgetTotal());
        assertEquals("New Manager", response.getManager().getFullName());
        
        verify(projectRepository).save(existingProject);
    }
}