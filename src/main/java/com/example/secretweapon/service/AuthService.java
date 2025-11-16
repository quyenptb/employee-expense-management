package com.example.secretweapon.service;


import com.example.secretweapon.exception.InvalidPasswordException;
import com.example.secretweapon.model.dto.AuthResponse;
import com.example.secretweapon.model.dto.LoginRequest;
import com.example.secretweapon.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {


    private final AuthenticationManager authenticationManager;


    private final JwtService jwtService;

    AuthService(AuthenticationManager authenticationManager, JwtService jwtService, PasswordValidatorService passwordValidatorService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
            }

    // (EPIC 01)
    public AuthResponse login(LoginRequest loginRequest) {

          Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );


            SecurityContextHolder.getContext().setAuthentication(authentication);

            // get user details
            User user = (User) authentication.getPrincipal();

            String jwt = jwtService.generateToken(user);

            //Get Role list
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return new AuthResponse(jwt, user.getEmail(), roles, user.getId());
        }

}