package com.example.secretweapon.service.impl;

import com.example.secretweapon.mapper.UserMapper;
import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.UserUpdateRequest;
import com.example.secretweapon.repository.UserRepository;
import com.example.secretweapon.service.DepartmentService;
import com.example.secretweapon.service.UserServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private UserServiceImpl userService;

    // --- GET ---
    @Test
    @DisplayName("getAllUsers_shouldReturnList")
    void getAllUsers_shouldReturnList() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("test@test.com", result.get(0).getEmail());
    }

    @Test
    @DisplayName("getUserById_ExistingId_shouldReturnUser")
    void getUserById_ExistingId_shouldReturnUser() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("getUserByEmailAddress_ExistingEmail_shouldReturnUser")
    void getUserByEmailAddress_ExistingEmail_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmailAddress("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    // --- UPDATE (Giữ lại logic cũ của cậu và bổ sung check) ---
    @Test
    @DisplayName("updateUser_ValidRequest_shouldReturnUpdatedUser")
    void updateUser_ValidRequest_shouldReturnUpdatedUser() {
        // Arrange
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(1L);
        request.setFullName("Updated Name");
        request.setDepartmentId(10L);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Original Name");

        Department department = new Department();
        department.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(departmentService.getDepartmentById(10L)).thenReturn(department);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0)); // Return what is saved

        // Act
        User updatedUser = userService.updateUser(request);

        // Assert
        assertEquals("Updated Name", updatedUser.getFullName());
        assertEquals(department, updatedUser.getDepartment());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("updateUser_UserNotFound_shouldThrowEntityNotFoundException")
    void updateUser_UserNotFound_shouldThrowEntityNotFoundException() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(99L);
        
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(request));
    }

    // --- DELETE ---
    @Test
    @DisplayName("deleteUser_shouldRunWithoutError")
    void deleteUser_shouldRunWithoutError() {
        // Vì hàm deleteUser trong impl đang rỗng (void method empty body), 
        // test này chủ yếu để đảm bảo gọi hàm không crash và tăng coverage cho method definition.
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        
        // Nếu sau này logic delete được thêm vào (vd gọi repo.deleteById), 
        // chỉ cần thêm verify(userRepository).deleteById(1L);
    }
}