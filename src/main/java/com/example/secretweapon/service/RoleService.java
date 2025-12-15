package com.example.secretweapon.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream()
                .collect(Collectors.toList());
    }

    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
}
