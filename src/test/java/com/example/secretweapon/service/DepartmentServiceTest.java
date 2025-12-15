package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.DepartmentRequest;
import com.example.secretweapon.repository.DepartmentRepository;
import com.example.secretweapon.repository.UserRepository;
import com.example.secretweapon.service.DepartmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    @DisplayName("createDepartment_ValidRequest_shouldReturnDepartment")
    void createDepartment_ValidRequest_shouldReturnDepartment() {
        // Arrange
        DepartmentRequest request = new DepartmentRequest();
        request.setName("IT Department");
        request.setManagerId(1L);

        User manager = new User();
        manager.setId(1L);

        Department savedDept = new Department();
        savedDept.setId(10L);
        savedDept.setName("IT Department");
        savedDept.setManager(manager);

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(departmentRepository.save(any(Department.class))).thenReturn(savedDept);

        // Act
        Department result = departmentService.createDepartment(request);

        // Assert
        assertNotNull(result);
        assertEquals("IT Department", result.getName());
        verify(userRepository).findById(1L);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("updateDepartment_ExistingId_shouldReturnUpdatedDepartment")
    void updateDepartment_ExistingId_shouldReturnUpdatedDepartment() {
        // Arrange
        Long deptId = 1L;
        DepartmentRequest request = new DepartmentRequest();
        request.setName("Updated Name");

        Department existingDept = new Department();
        existingDept.setId(deptId);
        existingDept.setName("Old Name");

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(existingDept));
        when(departmentRepository.save(any(Department.class))).thenReturn(existingDept);

        // Act
        Department result = departmentService.updateDepartment(deptId, request);

        // Assert
        assertEquals("Updated Name", result.getName());
        verify(departmentRepository).save(existingDept);
    }

    @Test
    @DisplayName("deleteDepartment_ExistingId_shouldDeleteDepartment")
    void deleteDepartment_ExistingId_shouldDeleteDepartment() {
        // Arrange
        Long deptId = 1L;
        Department existingDept = new Department();
        existingDept.setId(deptId);

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(existingDept));

        // Act
        departmentService.deleteDepartment(deptId);

        // Assert
        verify(departmentRepository).delete(existingDept);
    }

    @Test
    @DisplayName("getDepartmentById_NotFound_shouldThrowException")
    void getDepartmentById_NotFound_shouldThrowException() {
        // Arrange
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> departmentService.getDepartmentById(99L));
    }
}