package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.ZohoEmployeeDto;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.repository.RoleRepository;
import com.example.secretweapon.repository.UserRepository;
import com.example.secretweapon.service.ZohoService;
import com.example.secretweapon.service.ZohoSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZohoSyncServiceTest {

    @Mock
    private ZohoService zohoService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ZohoSyncService zohoSyncService;

    @Test
    @DisplayName("syncEmployeesFromZoho_NewEmployeesFound_shouldCreateNewUsers")
    void syncEmployeesFromZoho_NewEmployeesFound_shouldCreateNewUsers() {
        // Arrange
        ZohoEmployeeDto zohoUser = new ZohoEmployeeDto();
        zohoUser.setEmail("zoho@example.com");
        zohoUser.setFirstName("Zoho");
        zohoUser.setLastName("User");
        zohoUser.setStatus("Active");

        Role employeeRole = new Role();
        employeeRole.setName(RoleName.ROLE_EMPLOYEE);

        when(zohoService.fetchEmployees()).thenReturn(List.of(zohoUser));
        when(roleRepository.findByName(RoleName.ROLE_EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(userRepository.findByEmail("zoho@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");

        // Act
        String result = zohoSyncService.syncEmployeesFromZoho();

        // Assert
        assertTrue(result.contains("New: 1"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("syncEmployeesFromZoho_ExistingEmployeesFound_shouldUpdateUsers")
    void syncEmployeesFromZoho_ExistingEmployeesFound_shouldUpdateUsers() {
        // Arrange
        ZohoEmployeeDto zohoUser = new ZohoEmployeeDto();
        zohoUser.setEmail("exist@example.com");
        zohoUser.setFirstName("Zoho");
        zohoUser.setLastName("Updated");

        User existingUser = new User();
        existingUser.setEmail("exist@example.com");

        when(zohoService.fetchEmployees()).thenReturn(List.of(zohoUser));
        when(roleRepository.findByName(RoleName.ROLE_EMPLOYEE)).thenReturn(Optional.of(new Role()));
        when(userRepository.findByEmail("exist@example.com")).thenReturn(Optional.of(existingUser));

        // Act
        String result = zohoSyncService.syncEmployeesFromZoho();

        // Assert
        assertTrue(result.contains("Existing: 1"));
        assertEquals("Zoho Updated", existingUser.getFullName());
        verify(userRepository).save(existingUser);
    }
}