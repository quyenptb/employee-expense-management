package com.example.secretweapon.service;


import com.example.secretweapon.model.dto.CreateUserRequest;
import com.example.secretweapon.model.dto.UserDto;
import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.repository.RoleRepository;
import com.example.secretweapon.repository.UserRepository;
import com.example.secretweapon.repository.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Tạo user mới (EPIC 01)
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng: " + request.getEmail());
        }

        // Tìm Role
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + request.getRoleName()));

        User manager = null;
        // Nếu là EMPLOYEE, tìm manager
        if (request.getRoleName() == RoleName.ROLE_EMPLOYEE) {
            if (request.getManagerId() == null) {
                throw new BadRequestException("Employee phải có managerId");
            }
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Manager với ID: " + request.getManagerId()));

            // Kiểm tra xem manager có đúng là MANAGER không
            if(manager.getRole().getName() != RoleName.ROLE_MANAGER) {
                throw new BadRequestException("Người dùng (ID: " + request.getManagerId() + ") không phải là Manager");
            }
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa mật khẩu
        user.setRole(role);
        user.setManager(manager);
        user.setAvatarUrl(request.getAvatarUrl());

        User savedUser = userRepository.save(user);
        return mapToUserDto(savedUser);
    }

    // Lấy danh sách user (cho Admin)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Helper map Entity -> DTO
    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().getName().name());
        if (user.getManager() != null) {
            dto.setManagerName(user.getManager().getFullName());
        }
        return dto;
    }
}