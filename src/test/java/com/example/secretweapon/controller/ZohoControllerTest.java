package com.example.secretweapon.controller;

import com.example.secretweapon.service.ZohoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ZohoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ZohoService zohoService;

    @InjectMocks
    private ZohoController zohoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(zohoController).build();
    }

    @Test
    @DisplayName("authorize_shouldRedirectToZohoAuthUrl")
    void authorize_shouldRedirectToZohoAuthUrl() throws Exception {
        // Arrange
        String authUrl = "https://accounts.zoho.com/oauth/v2/auth?client_id=xyz";
        when(zohoService.generateAuthorizationUrl()).thenReturn(authUrl);

        // Act & Assert
        mockMvc.perform(get("/api/zoho/authorize"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(authUrl));
    }

    @Test
    @DisplayName("callback_SuccessCode_shouldRedirectToAdminSuccess")
    void callback_SuccessCode_shouldRedirectToAdminSuccess() throws Exception {
        // Arrange
        String code = "valid_code";
        doNothing().when(zohoService).exchangeCodeForToken(code);

        // Act & Assert
        mockMvc.perform(get("/api/zoho/callback")
                        .param("code", code))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:3000/admin?zoho_status=success"));
    }

    @Test
    @DisplayName("callback_WithErrorParam_shouldRedirectToAdminError")
    void callback_WithErrorParam_shouldRedirectToAdminError() throws Exception {
        // Arrange
        String errorMsg = "access_denied";

        // Act & Assert
        mockMvc.perform(get("/api/zoho/callback")
                        .param("code", "any")
                        .param("error", errorMsg))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:3000/admin?zoho_status=error&message=" + errorMsg));
    }

    @Test
    @DisplayName("callback_ExceptionDuringExchange_shouldRedirectToAdminError")
    void callback_ExceptionDuringExchange_shouldRedirectToAdminError() throws Exception {
        // Arrange
        String code = "invalid_code";
        doThrow(new RuntimeException("Token invalid")).when(zohoService).exchangeCodeForToken(code);

        // Act & Assert
        mockMvc.perform(get("/api/zoho/callback")
                        .param("code", code))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:3000/admin?zoho_status=error&message=Token invalid"));
    }
}