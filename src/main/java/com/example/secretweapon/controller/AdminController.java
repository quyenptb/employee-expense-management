package com.example.secretweapon.controller;


import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.payload.request.UserCreateRequest;
import com.example.secretweapon.payload.response.UserResponse;
import com.example.secretweapon.service.AdminService;
import com.example.secretweapon.service.ProjectSyncService;
import com.example.secretweapon.service.RoleService;
import com.example.secretweapon.service.ZohoSyncService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    
    private final AdminService adminService;

    private final ProjectSyncService projectSyncService;

    private final RoleService roleService;

    private final ZohoSyncService zohoSyncService; 

    // API Tạo user mới (EPIC 01)
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest createUserRequest) {
        UserResponse newUser = adminService.createUser(createUserRequest);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // API Lấy danh sách user (EPIC 01)
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/jobTitles")
    public ResponseEntity<JobTitle[]> getAllJobTitle() {
        JobTitle[] jobTitles = JobTitle.values();
        return ResponseEntity.ok(jobTitles);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roleNames = roleService.getAllRoles();
        return ResponseEntity.ok(roleNames);
    }  

    //localhost:8000/api/admin/projects/sync   

    @PostMapping("/projects/sync")
    public ResponseEntity<String> syncJiraProjects() {
        try {
            projectSyncService.syncProjects();
            return ResponseEntity.ok("Sync Jira project successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error when sync: " + e.getMessage());
        }
    }

    @PostMapping("/zoho/sync-employees")
    public ResponseEntity<String> syncZohoEmployees() {
        try {
            String result = zohoSyncService.syncEmployeesFromZoho();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error syncing Zoho employees: " + e.getMessage());
        }
    }



}
