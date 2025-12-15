package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.ZohoEmployeeDto;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.model.enums.UserStatus;
import com.example.secretweapon.repository.RoleRepository;
import com.example.secretweapon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZohoSyncService {

    private final ZohoService zohoService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String syncEmployeesFromZoho() {
        List<ZohoEmployeeDto> zohoEmployees = zohoService.fetchEmployees();
        
        if (zohoEmployees.isEmpty()) {
            return "No employees found on Zoho or View Name incorrect.";
        }

        int countNew = 0;
        int countUpdated = 0;
        
        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Role EMPLOYEE not found in DB"));

        for (ZohoEmployeeDto zUser : zohoEmployees) {

            if (zUser.getEmail() == null || zUser.getEmail().isEmpty()) continue;

            Optional<User> existingUserOpt = userRepository.findByEmail(zUser.getEmail());

            if (existingUserOpt.isPresent()) {
            
                User existingUser = existingUserOpt.get();
                
                existingUser.setFullName(zUser.getFullName());
                userRepository.save(existingUser);
                countUpdated++; // (Dù chưa update gì nhiều nhưng cứ đếm là đã check)
            } else {
                // Tạo User mới
                User newUser = new User();
                newUser.setFullName(zUser.getFullName());
                newUser.setEmail(zUser.getEmail());
                
                // Password mặc định: "Zoho@123" (Hoặc random)
                newUser.setPassword(passwordEncoder.encode("Zoho@123")); 
                
                newUser.setRole(employeeRole);
                newUser.setStatus("Active".equalsIgnoreCase(zUser.getStatus()) ? UserStatus.ACTIVE : UserStatus.LOCKED);
                
                // Lưu Zoho Record ID vào metadata (nếu User entity hỗ trợ JSON metadata)
                String metadata = String.format("{\"zohoRecordId\": \"%s\", \"employeeId\": \"%s\"}", 
                        zUser.getRecordId(), zUser.getEmployeeId());
                newUser.setMetadata(metadata);

                userRepository.save(newUser);
                countNew++;
            }
        }
        
        return String.format("Sync completed. Found: %d. New: %d. Existing: %d.", 
                zohoEmployees.size(), countNew, countUpdated);
    }
}