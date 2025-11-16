package com.example.secretweapon.service;

import org.springframework.stereotype.Service;

//For admin when creating employee accounts
@Service
public class PasswordValidatorService {
    public boolean isValid(String password) {
        return !hasShortPassword(password) && hasMoreThanOneUpperCase(password) && hasAtLeastOneNumber(password);
    }

    private boolean hasShortPassword(String password) {
        return password.length() < 8;
    }

    private boolean hasMoreThanOneUpperCase(String password) {
        int count = (int) password.chars().filter(Character::isUpperCase).count();
        return count > 1;
    }

    private boolean hasAtLeastOneNumber(String password) {
        int count = (int) password.chars().filter(Character::isDigit).count();
        return count >= 1;
    }
}
