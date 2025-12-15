package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.ZohoEmployeeDto;
import com.example.secretweapon.model.entity.ThirdPartyToken;
import com.example.secretweapon.repository.ThirdPartyTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZohoServiceTest {

    @Mock
    private ThirdPartyTokenRepository tokenRepository;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ZohoService zohoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(zohoService, "clientId", "client-id");
        ReflectionTestUtils.setField(zohoService, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(zohoService, "redirectUri", "http://callback");
        ReflectionTestUtils.setField(zohoService, "accountsUrl", "https://accounts.zoho.com");
        ReflectionTestUtils.setField(zohoService, "apiUrl", "https://people.zoho.com/api");
    }

    private void mockWebClientChain() {
        // Mock chain: builder.build() -> client
        //when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
    }
    
    private void mockWebClientGetChain() {
         mockWebClientChain();
         when(webClient.get()).thenReturn(requestHeadersUriSpec);
         when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
         when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
         when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("generateAuthorizationUrl_shouldReturnUrl")
    void generateAuthorizationUrl_shouldReturnUrl() {
        String url = zohoService.generateAuthorizationUrl();
        assertNotNull(url);
        assertEquals("https://accounts.zoho.com/oauth/v2/auth?scope=null&client_id=client-id&response_type=code&access_type=offline&prompt=consent&redirect_uri=http://callback", url);
    }

    @Test
    @DisplayName("fetchEmployees_ValidToken_shouldReturnList")
    void fetchEmployees_ValidToken_shouldReturnList() {
        // Arrange
        ThirdPartyToken token = new ThirdPartyToken();
        token.setAccessToken("valid-token");
        token.setExpiresInSeconds(3600L);
        token.setTokenCreatedAt(LocalDateTime.now());
        
        when(tokenRepository.findByProvider("ZOHO")).thenReturn(Optional.of(token));

        // Mock WebClient GET call
        mockWebClientGetChain();
        
        ZohoEmployeeDto emp = new ZohoEmployeeDto();
        emp.setFirstName("Zoho");
        
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of(emp)));

        // Act
        List<ZohoEmployeeDto> result = zohoService.fetchEmployees();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Zoho", result.get(0).getFirstName());
    }
}