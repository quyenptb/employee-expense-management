package com.example.secretweapon.controller;

import com.example.secretweapon.payload.request.LoginRequest;
import com.example.secretweapon.payload.response.AuthResponse;
import com.example.secretweapon.service.AuthService;
import com.example.secretweapon.service.PasswordValidatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;



    AuthController(AuthService authService) {

        this.authService = authService;
    }

    // API login (EPIC 01)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

            AuthResponse authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);


    }

}