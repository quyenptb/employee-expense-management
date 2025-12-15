package com.example.secretweapon.controller;

import com.example.secretweapon.service.ZohoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/zoho")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ZohoController {

    private final ZohoService zohoService;

    // Bước 1: Frontend gọi API này, Backend trả về URL để redirect user sang Zoho
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String authUrl = zohoService.generateAuthorizationUrl();
        // Redirect trực tiếp browser sang Zoho
        response.sendRedirect(authUrl);
    }

    // Bước 2: Zoho redirect về đây kèm theo 'code'
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code, 
                         @RequestParam(value = "error", required = false) String error,
                         HttpServletResponse response) throws IOException {
        
        if (error != null) {
            response.sendRedirect("http://localhost:3000/admin?zoho_status=error&message=" + error);
            return;
        }

        try {
            zohoService.exchangeCodeForToken(code);
            // Thành công -> Redirect về trang Admin của Frontend
            response.sendRedirect("http://localhost:3000/admin?zoho_status=success");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/admin?zoho_status=error&message=" + e.getMessage());
        }
    }
    
    // API Test thử xem token có sống không
    @GetMapping("/token/status")
    public ResponseEntity<String> checkTokenStatus() {
        try {
            String token = zohoService.getValidAccessToken();
            return ResponseEntity.ok("Token is valid. Access Token: " + token.substring(0, 10) + "...");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalid or missing.");
        }
    }

    
}