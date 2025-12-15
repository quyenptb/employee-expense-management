package com.example.secretweapon.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.service.QuickBooksService;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/qbo")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QuickBooksOAuthController {

    @Value("${intuit.oauth2.clientId}")
    private String clientId;

    @Value("${intuit.oauth2.redirectUri}")
    private String redirectUri;

    @Value("${intuit.oauth2.authorizationUrl}")
    private String authorizationUrl;


    private final QuickBooksService quickBooksService;

    // Endpoint 1: Bắt đầu quá trình kết nối
    @GetMapping("/connect")
    public RedirectView connectToQuickBooks() {
        String scope = "com.intuit.quickbooks.accounting openid profile email phone address"; // Yêu cầu các quyền cần thiết
        String state = "random_state_string"; // Tùy chọn: dùng để chống tấn công CSRF

        String url = authorizationUrl +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + state;

        return new RedirectView(url);
    }

    // Endpoint 2: Xử lý Callback từ Intuit (phải khớp với Redirect URI)
    @GetMapping("/oauth2redirect")
    public void handleOAuth2Redirect(@RequestParam("code") String code,
                                       @RequestParam("state") String state,
                                       @RequestParam(value = "realmId", required = false 
                                       ) String realmId,
                                       HttpServletResponse response
                                    
                                    ) throws IOException{

        // TODO: Xác minh 'state' để đảm bảo an toàn

                try {
            //log.info("Received Code: {}, RealmId: {}", code, realmId);
            quickBooksService.exchangeCodeForToken(code, realmId);
            // Thành công -> Redirect về trang Admin của Frontend
            response.sendRedirect("http://localhost:3000/admin?qbo_status=success");
        } catch (Exception e) {
            //log.error("Failed to exchange code for token", e);
            response.sendRedirect("http://localhost:3000/admin?qbo_status=error&message=" + e.getMessage());
        }
    }

    


}