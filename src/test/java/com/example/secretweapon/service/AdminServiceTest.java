package com.example.secretweapon.service;

import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.UserMapper;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.payload.request.UserCreateRequest;
import com.example.secretweapon.payload.response.UserResponse;
import com.example.secretweapon.repository.RoleRepository;
import com.example.secretweapon.repository.UserRepository;
import com.example.secretweapon.repository.DepartmentRepository;
import com.example.secretweapon.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminService adminService;

    @Test
    void createUser_Success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("new@example.com");
        request.setFullName("New Employee");
        request.setPassword("Password123");
        request.setRoleName(RoleName.ROLE_EMPLOYEE);
        request.setDepartmentId(100L);
        request.setManagerId(100L);

        Role roleEmployee = new Role();
        roleEmployee.setName(RoleName.ROLE_EMPLOYEE);

        Department departmentEmployee = new Department();
        departmentEmployee.setId(100L);
        departmentEmployee.setName("Phong Hanh Chinh");

        User managerUser = new User();
        managerUser.setId(100L);
        Role roleManager = new Role();
        roleManager.setName(RoleName.ROLE_MANAGER);
        managerUser.setRole(roleManager);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("new@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_EMPLOYEE)).thenReturn(Optional.of(roleEmployee));
        when(departmentRepository.findById(100L)).thenReturn(Optional.of(departmentEmployee));
        when(userRepository.findById(100L)).thenReturn(Optional.of(managerUser));

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse response = adminService.createUser(request);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_Fail_EmailExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("exist@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> adminService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_Fail_InvalidManager() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("emp@example.com");
        request.setRoleName(RoleName.ROLE_EMPLOYEE);
        request.setManagerId(99L);

        Role roleEmployee = new Role();
        roleEmployee.setName(RoleName.ROLE_EMPLOYEE);

        User fakeManager = new User();
        fakeManager.setId(99L);
        Role roleFake = new Role();
        roleFake.setName(RoleName.ROLE_EMPLOYEE);
        fakeManager.setRole(roleFake);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_EMPLOYEE)).thenReturn(Optional.of(roleEmployee));
        //when(userRepository.findById(99L)).thenReturn(Optional.of(fakeManager));

        assertThrows(ResourceNotFoundException.class, () -> adminService.createUser(request));
    }
}