package com.example.secretweapon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.secretweapon.model.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>{
    
}
