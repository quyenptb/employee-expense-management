package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    @DisplayName("getAllRoles_shouldReturnList")
    void getAllRoles_shouldReturnList() {
        // Arrange
        Role role = new Role();
        role.setName(RoleName.ROLE_ADMIN);
        when(roleRepository.findAll()).thenReturn(List.of(role));

        // Act
        List<Role> result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RoleName.ROLE_ADMIN, result.get(0).getName());
    }

    @Test
    @DisplayName("getRoleById_ExistingId_shouldReturnRole")
    void getRoleById_ExistingId_shouldReturnRole() {
        // Arrange
        Long id = 1L;
        Role role = new Role();
        role.setId(id);
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));

        // Act
        Optional<Role> result = roleService.getRoleById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    @DisplayName("getRoleById_NotFound_shouldReturnEmpty")
    void getRoleById_NotFound_shouldReturnEmpty() {
        // Arrange
        Long id = 99L;
        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Role> result = roleService.getRoleById(id);

        // Assert
        assertTrue(result.isEmpty());
    }
}