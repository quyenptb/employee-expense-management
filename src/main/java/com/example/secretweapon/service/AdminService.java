package com.example.secretweapon.service;


import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.UserMapper;
import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.payload.request.UserCreateRequest;
import com.example.secretweapon.payload.response.UserResponse;
import com.example.secretweapon.repository.DepartmentRepository;
import com.example.secretweapon.repository.RoleRepository;
import com.example.secretweapon.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final DepartmentRepository departmentRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    // Tạo user mới (EPIC 01)
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng: " + request.getEmail());
        }

        //Find Role
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + request.getRoleName()));

        //Find Department
        Department department = departmentRepository.findById(request.getDepartmentId())
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Department: " + request.getDepartmentId()));

        User manager = null;
        // Nếu là EMPLOYEE, tìm manager
        if (request.getRoleName() == RoleName.ROLE_EMPLOYEE) {
            if (request.getManagerId() == null) {
                throw new BadRequestException("Employee phải có managerId");
            }
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Manager với ID: " + request.getManagerId()));
            
            if(manager.getRole().getName() != RoleName.ROLE_MANAGER) {
                throw new BadRequestException("Người dùng (ID: " + request.getManagerId() + ") không phải là Manager");
            }
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa mật khẩu
        user.setRole(role);
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle()); }
        user.setManager(manager);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setDepartment(department);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    

    // Lấy danh sách user (cho Admin)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    
}