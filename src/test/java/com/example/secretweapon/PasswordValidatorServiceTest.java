package com.example.secretweapon;


import com.example.secretweapon.service.PasswordValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Disabled
public class PasswordValidatorServiceTest {

    private PasswordValidatorService passwordValidatorService;

    @BeforeEach
    void setUp() {
        passwordValidatorService = new PasswordValidatorService();
    }

    @Test
    @DisplayName("Too short password")
    void shouldReturnFalse_TooShortPassword() {
            String password = "1234567";
            boolean result = passwordValidatorService.isValid(password);
            assertFalse(result);
    }

    @Test
    @DisplayName("More than one uppercase")
    void shouldReturnTrue_WhenPasswordHasMoreThanOneUpperCase() {
        String password = "12QQbbbbbbbbbb";
        boolean result = passwordValidatorService.isValid(password);
        assertTrue(result);
    }

    @Test
    @DisplayName("Has atleast one number")
    void shouldReturnTrue_WhenPasswordHasAtLeastOneNumber() {
        String password = "12QQmdfdjfjjgkdss";
        boolean result = passwordValidatorService.isValid(password);
        assertTrue(result);
    }

}
