package com.example.secretweapon.controller;


import com.example.secretweapon.controller.AdminController;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.payload.request.UserCreateRequest;
import com.example.secretweapon.payload.response.UserResponse;
import com.example.secretweapon.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass Security Filters for Unit Test simplicity
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;
    @MockBean
    private ProjectSyncService projectSyncService;
    @MockBean
    private RoleService roleService;
    @MockBean
    private ZohoSyncService zohoSyncService;

    // Mock beans required by SecurityConfig/GlobalHandlers even if filters are off
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("createUser_ValidRequest_shouldReturnCreated")
    void createUser_ValidRequest_shouldReturnCreated() throws Exception {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setFullName("New User");
        request.setEmail("new@test.com");
        request.setPassword("pass");
        request.setRoleName(RoleName.ROLE_EMPLOYEE);

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("new@test.com");

        when(adminService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    @DisplayName("getAllUsers_shouldReturnList")
    void getAllUsers_shouldReturnList() throws Exception {
        // Arrange
        UserResponse u1 = new UserResponse();
        u1.setFullName("U1");
        when(adminService.getAllUsers()).thenReturn(List.of(u1));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("U1"));
    }

    @Test
    @DisplayName("getAllRoles_shouldReturnRoles")
    void getAllRoles_shouldReturnRoles() throws Exception {
        // Arrange
        Role role = new Role();
        role.setName(RoleName.ROLE_ADMIN);
        when(roleService.getAllRoles()).thenReturn(List.of(role));

        // Act & Assert
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ROLE_ADMIN"));
    }
}