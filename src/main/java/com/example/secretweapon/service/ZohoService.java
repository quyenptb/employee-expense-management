package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.ZohoEmployeeDto;
import com.example.secretweapon.model.entity.ThirdPartyToken;
import com.example.secretweapon.repository.ThirdPartyTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZohoService {

    private final ThirdPartyTokenRepository tokenRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${zoho.client-id}")
    private String clientId;

    @Value("${zoho.client-secret}")
    private String clientSecret;

    @Value("${zoho.redirect-uri}")
    private String redirectUri;

    @Value("${zoho.accounts-url}")
    private String accountsUrl;

    @Value("${zoho.scopes}")
    private String scopes;

    @Value("${zoho.api-url}") 
    private String apiUrl;

    // 1. Tạo URL để Frontend redirect user sang Zoho Login
    public String generateAuthorizationUrl() {
        return accountsUrl + "/oauth/v2/auth?" +
                "scope=" + scopes +
                "&client_id=" + clientId +
                "&response_type=code" +
                "&access_type=offline" + // Quan trọng để lấy Refresh Token
                "&prompt=consent" + 
                "&redirect_uri=" + redirectUri;
    }

    // 2. Xử lý Callback: Đổi Code lấy Token
    @Transactional
    public void exchangeCodeForToken(String code) {
        WebClient webClient = webClientBuilder.baseUrl(accountsUrl).build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        JsonNode response = webClient.post()
                .uri("/oauth/v2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("access_token")) {
            saveToken(response);
        } else {
            throw new RuntimeException("Failed to retrieve token from Zoho: " + (response != null ? response.toPrettyString() : "null"));
        }
    }

    private void saveToken(JsonNode response) {
        String accessToken = response.get("access_token").asText();
        // Refresh token chỉ được trả về lần đầu tiên hoặc khi access_type=offline + prompt=consent
        String refreshToken = response.has("refresh_token") ? response.get("refresh_token").asText() : null;
        Long expiresIn = response.get("expires_in").asLong();

        ThirdPartyToken token = tokenRepository.findByProvider("ZOHO")
                .orElse(ThirdPartyToken.builder().provider("ZOHO").build());

        token.setAccessToken(accessToken);
        if (refreshToken != null) {
            token.setRefreshToken(refreshToken);
        }
        token.setExpiresInSeconds(expiresIn);
        token.setTokenCreatedAt(LocalDateTime.now());

        tokenRepository.save(token);
        log.info("Zoho tokens saved successfully.");
    }

    // 3. Lấy Access Token (Tự động refresh nếu hết hạn) - Dùng cái này khi gọi API
    public String getValidAccessToken() {
        ThirdPartyToken token = tokenRepository.findByProvider("ZOHO")
                .orElseThrow(() -> new RuntimeException("No Zoho token found. Please connect Zoho first."));

        // Kiểm tra hết hạn (trừ hao 60s)
        if (token.getTokenCreatedAt().plusSeconds(token.getExpiresInSeconds() - 60).isBefore(LocalDateTime.now())) {
            log.info("Zoho access token expired. Refreshing...");
            refreshAccessToken(token);
        }

        return token.getAccessToken();
    }

    private void refreshAccessToken(ThirdPartyToken token) {
        if (token.getRefreshToken() == null) {
            throw new RuntimeException("No refresh token available. Please re-authorize Zoho.");
        }

        WebClient webClient = webClientBuilder.baseUrl(accountsUrl).build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("refresh_token", token.getRefreshToken());
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "refresh_token");

        JsonNode response = webClient.post()
                .uri("/oauth/v2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("access_token")) {
            token.setAccessToken(response.get("access_token").asText());
            token.setExpiresInSeconds(response.get("expires_in").asLong());
            token.setTokenCreatedAt(LocalDateTime.now());
            tokenRepository.save(token);
            log.info("Zoho access token refreshed successfully.");
        } else {
            throw new RuntimeException("Failed to refresh token.");
        }
    }

    public List<ZohoEmployeeDto> fetchEmployees() {
        String accessToken = getValidAccessToken();
        
        // URL theo file bạn gửi: https://people.zoho.com/api/forms/P_EmployeeView/records
        // Lưu ý: apiUrl trong application.properties nên là 'https://people.zoho.com/api'
        String url = apiUrl + "/forms/P_EmployeeView/records"; 

        try {
            WebClient webClient = webClientBuilder.build();
            List<ZohoEmployeeDto> employees = webClient.get()
                    .uri(url)
                    .header("Authorization", "Zoho-oauthtoken " + accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ZohoEmployeeDto>>() {})
                    .block();
            
            return employees != null ? employees : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching employees from Zoho: ", e);
            throw new RuntimeException("Failed to fetch employees: " + e.getMessage());
        }
    }
}