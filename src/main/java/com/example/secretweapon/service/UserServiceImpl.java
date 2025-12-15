package com.example.secretweapon.service;

import com.example.secretweapon.mapper.UserMapper;
import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.UserUpdateRequest;
import com.example.secretweapon.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final DepartmentService departmentService;

    @Override
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> listOfUsers = userRepository.findAll();
        return listOfUsers;
    }

    @Override
    public User getUserByEmailAddress(String email) {
        return userRepository.findByEmail(email).get();
    }

    @Override
    public User updateUser(UserUpdateRequest userUpdate) {
        if (userUpdate.getUserId() == null) throw new IllegalArgumentException();
        User existingUser = userRepository.findById(userUpdate.getUserId())
    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userUpdate.getUserId()));
        /*
            private String fullName;
    private String email;
    private String role;
    private Long managerId;
    private String avatarUrl;
    private Long departmentId;
    private JobTitle jobTitle;
    private UserStatus status; */
    if (userUpdate.getFullName() != null) existingUser.setFullName(userUpdate.getFullName());
    if (userUpdate.getEmail() != null) existingUser.setEmail(userUpdate.getEmail());
    if (userUpdate.getManagerId() != null ) {
        User manager = userRepository.findById(userUpdate.getManagerId())
            .orElseThrow(() -> new EntityNotFoundException("Manager not found with ID: " + userUpdate.getManagerId()));
        existingUser.setManager(manager);
    }
    if (userUpdate.getAvatarUrl() != null) existingUser.setAvatarUrl(userUpdate.getAvatarUrl());
    if (userUpdate.getDepartmentId() != null) {
        Department department = departmentService.getDepartmentById(userUpdate.getDepartmentId());
        existingUser.setDepartment(department);
    }
    if (userUpdate.getJobTitle() != null) existingUser.setJobTitle(userUpdate.getJobTitle());
    if (userUpdate.getStatus() != null) existingUser.setStatus(userUpdate.getStatus());

    return userRepository.save(existingUser);        
    }

    @Override
    public void deleteUser(long id) {

    }
}
