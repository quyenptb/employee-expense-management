package com.example.secretweapon.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.model.entity.Department;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.DepartmentRequest;
import com.example.secretweapon.repository.DepartmentRepository;
import com.example.secretweapon.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id).orElseThrow(
            () -> {
                return new EntityNotFoundException("Can not find Department");
    });
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        Department dept = new Department();
        dept.setName(request.getName());
        
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            dept.setManager(manager);
        }
        
        return departmentRepository.save(dept);
    }

    @Transactional
    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department dept = getDepartmentById(id);
        dept.setName(request.getName());

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            dept.setManager(manager);
        } else {
            dept.setManager(null);
        }

        return departmentRepository.save(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = getDepartmentById(id);
        departmentRepository.delete(dept);
    }

}
