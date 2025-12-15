package com.example.secretweapon.controller;

import com.example.secretweapon.mapper.DepartmentMapper;
import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.payload.request.DepartmentRequest;
import com.example.secretweapon.payload.response.DepartmentResponse;
import com.example.secretweapon.service.DepartmentService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DepartmentService departmentService;
    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();
    }

    @Test
    @DisplayName("getAllDepartments_shouldReturnList")
    void getAllDepartments_shouldReturnList() throws Exception {
        Department dept = new Department();
        dept.setId(1L);
        DepartmentResponse res = new DepartmentResponse(1L, "IT", null, "Manager");

        when(departmentService.getAllDepartments()).thenReturn(List.of(dept));
        when(departmentMapper.toResponse(dept)).thenReturn(res);

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IT"));
    }

    @Test
    @DisplayName("createDepartment_Valid_shouldReturnCreated")
    void createDepartment_Valid_shouldReturnCreated() throws Exception {
        DepartmentRequest req = new DepartmentRequest();
        req.setName("HR");
        
        Department dept = new Department();
        dept.setId(2L);
        DepartmentResponse res = new DepartmentResponse(2L, "HR", null, null);

        when(departmentService.createDepartment(any(DepartmentRequest.class))).thenReturn(dept);
        when(departmentMapper.toResponse(dept)).thenReturn(res);

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HR"));
    }

    @Test
    @DisplayName("updateDepartment_Valid_shouldReturnUpdated")
    void updateDepartment_Valid_shouldReturnUpdated() throws Exception {
        Long id = 1L;
        DepartmentRequest req = new DepartmentRequest();
        req.setName("IT Updated");

        Department dept = new Department();
        DepartmentResponse res = new DepartmentResponse(id, "IT Updated", null, null);

        when(departmentService.updateDepartment(eq(id), any())).thenReturn(dept);
        when(departmentMapper.toResponse(dept)).thenReturn(res);

        mockMvc.perform(put("/api/departments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT Updated"));
    }

    @Test
    @DisplayName("deleteDepartment_Valid_shouldReturnNoContent")
    void deleteDepartment_Valid_shouldReturnNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(departmentService).deleteDepartment(id);

        mockMvc.perform(delete("/api/departments/{id}", id))
                .andExpect(status().isNoContent());
    }
}