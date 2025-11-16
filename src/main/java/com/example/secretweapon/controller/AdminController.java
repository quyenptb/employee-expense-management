package com.example.secretweapon.controller;


import com.example.secretweapon.model.dto.CreateUserRequest;
import com.example.secretweapon.model.dto.UserDto;
import com.example.secretweapon.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // API Tạo user mới (EPIC 01)
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserDto newUser = adminService.createUser(createUserRequest);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // API Lấy danh sách user (EPIC 01)
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
