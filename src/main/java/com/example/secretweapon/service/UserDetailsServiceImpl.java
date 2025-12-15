package com.example.secretweapon.service;

import com.example.secretweapon.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Dùng cho Spring Security lấy User details từ DB
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // is is username (EPIC 01)
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user with email: " + email));
    }
}