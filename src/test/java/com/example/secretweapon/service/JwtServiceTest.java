package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.RoleName;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    // A valid 256-bit secret key for HMAC-SHA256 (32 bytes encoded in Base64)
    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        // Inject values for @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);
    }

    @Test
    @DisplayName("generateToken_ValidUser_shouldReturnToken")
    void generateToken_ValidUser_shouldReturnToken() {
        // Arrange
        Role role = new Role(Long.valueOf(1L), RoleName.ROLE_EMPLOYEE);
        User user = new User();
        user.setEmail("user@test.com");
        user.setId(1L);
        user.setFullName("Test User");
        user.setRole(role);
        

        // Act
        String token = jwtService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("extractUsername_ValidToken_shouldReturnEmail")
    void extractUsername_ValidToken_shouldReturnEmail() {
        // Arrange
        Role role = new Role(Long.valueOf(1L), RoleName.ROLE_EMPLOYEE);
        User user = new User();
        user.setEmail("user@test.com");
        user.setId(1L);
        user.setRole(role);
        String token = jwtService.generateToken(user);

        // Act
        String extractedEmail = jwtService.extractUsername(token);

        // Assert
        assertEquals("user@test.com", extractedEmail);
    }

    @Test
    @DisplayName("isTokenValid_ValidTokenAndUser_shouldReturnTrue")
    void isTokenValid_ValidTokenAndUser_shouldReturnTrue() {
        // Arrange
        Role role = new Role(Long.valueOf(1L), RoleName.ROLE_EMPLOYEE);
        User user = new User();
        user.setEmail("user@test.com");
        user.setId(1L);
        user.setRole(role);

        String token = jwtService.generateToken(user);

        // Act
        boolean isValid = jwtService.isTokenValid(token, user);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid_WrongUser_shouldReturnFalse")
    void isTokenValid_WrongUser_shouldReturnFalse() {
        // Arrange
        Role role1 = new Role(Long.valueOf(1L), RoleName.ROLE_EMPLOYEE);
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setId(1L);
        user1.setRole(role1);
        
        Role role2 = new Role(Long.valueOf(1L), RoleName.ROLE_EMPLOYEE);
        User user2 = new User();
        user2.setEmail("user2@test.com"); // Different email
        user2.setRole(role2);

        String token = jwtService.generateToken(user1);

        // Act
        boolean isValid = jwtService.isTokenValid(token, user2);

        // Assert
        assertFalse(isValid);
    }
}