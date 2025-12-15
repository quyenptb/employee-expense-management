package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.ThirdPartyToken;
import com.example.secretweapon.repository.ThirdPartyTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuickBooksServiceTest {

    @Mock
    private ThirdPartyTokenRepository tokenRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QuickBooksService quickBooksService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields
        ReflectionTestUtils.setField(quickBooksService, "authorizationUrl", "https://appcenter.intuit.com/connect/oauth2");
        ReflectionTestUtils.setField(quickBooksService, "clientId", "my-client-id");
        ReflectionTestUtils.setField(quickBooksService, "redirectUri", "http://localhost:8080/callback");
        ReflectionTestUtils.setField(quickBooksService, "scopes", "accounting");
    }

    @Test
    @DisplayName("generateAuthorizationUrl_shouldReturnCorrectUrl")
    void generateAuthorizationUrl_shouldReturnCorrectUrl() {
        // Act
        String url = quickBooksService.generateAuthorizationUrl();

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("https://appcenter.intuit.com/connect/oauth2"));
        assertTrue(url.contains("client_id=my-client-id"));
        assertTrue(url.contains("redirect_uri=http://localhost:8080/callback"));
    }

    @Test
    @DisplayName("isConnected_TokenExists_shouldReturnTrue")
    void isConnected_TokenExists_shouldReturnTrue() {
        // Arrange
        when(tokenRepository.findByProvider("QUICKBOOKS"))
                .thenReturn(Optional.of(new ThirdPartyToken()));

        // Act
        boolean result = quickBooksService.isConnected();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isConnected_TokenMissing_shouldReturnFalse")
    void isConnected_TokenMissing_shouldReturnFalse() {
        // Arrange
        when(tokenRepository.findByProvider("QUICKBOOKS"))
                .thenReturn(Optional.empty());

        // Act
        boolean result = quickBooksService.isConnected();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("getValidTokenEntity_TokenValid_shouldReturnToken")
    void getValidTokenEntity_TokenValid_shouldReturnToken() {
        // Arrange
        ThirdPartyToken token = new ThirdPartyToken();
        token.setAccessToken("valid-access-token");
        token.setExpiresInSeconds(3600L); // 1 hour
        token.setTokenCreatedAt(LocalDateTime.now()); // Just created

        when(tokenRepository.findByProvider("QUICKBOOKS")).thenReturn(Optional.of(token));

        // Act
        ThirdPartyToken result = quickBooksService.getValidTokenEntity();

        // Assert
        assertEquals("valid-access-token", result.getAccessToken());
    }

    @Test
    @DisplayName("getValidTokenEntity_NoToken_shouldThrowException")
    void getValidTokenEntity_NoToken_shouldThrowException() {
        // Arrange
        when(tokenRepository.findByProvider("QUICKBOOKS")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> quickBooksService.getValidTokenEntity());
    }
}