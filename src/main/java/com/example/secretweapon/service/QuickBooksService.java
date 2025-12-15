package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.ThirdPartyToken;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.repository.ThirdPartyTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuickBooksService {

    private static final String PROVIDER_NAME = "QUICKBOOKS";
    
    private final ThirdPartyTokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    @Value("${intuit.oauth2.client-id}")
    private String clientId;

    @Value("${intuit.oauth2.client-secret}")
    private String clientSecret;

    @Value("${intuit.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${intuit.oauth2.authorization-url}")
    private String authorizationUrl;

    @Value("${intuit.oauth2.token-url}")
    private String tokenUrl;
    
    @Value("${intuit.oauth2.api-url}")
    private String apiUrl; 

    @Value("${intuit.oauth2.scopes}")
    private String scopes;

    @Value("${intuit.accounting.default-expense-account-id:33}") 
    private String defaultExpenseAccountId; 

    private WebClient getWebClient() {
        HttpClient httpClient = HttpClient.create().compress(false);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // --- 1. AUTH FLOW ---
    public String generateAuthorizationUrl() {
        return authorizationUrl + "?" +
                "scope=" + scopes +
                "&client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&state=security_token_string"; 
    }

    @Transactional
    public void exchangeCodeForToken(String code, String realmId) {
        String encoding = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        WebClient webClient = getWebClient();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");
        
        try {
            JsonNode response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Basic " + encoding) 
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("access_token")) {
                saveToken(response, realmId);
            } else {
                throw new RuntimeException("QuickBooks Auth Failed: No access token");
            }
        } catch (Exception e) {
            log.error("Error exchanging code", e);
            throw new RuntimeException("Failed to connect to QuickBooks: " + e.getMessage());
        }
    }

    private void saveToken(JsonNode response, String realmId) {
        String accessToken = response.get("access_token").asText();
        String refreshToken = response.has("refresh_token") ? response.get("refresh_token").asText() : null;
        Long expiresIn = response.get("expires_in").asLong();

        ThirdPartyToken token = tokenRepository.findByProvider(PROVIDER_NAME)
                .orElse(ThirdPartyToken.builder().provider(PROVIDER_NAME).build());

        token.setAccessToken(accessToken);
        if (refreshToken != null) token.setRefreshToken(refreshToken);
        token.setExpiresInSeconds(expiresIn);
        token.setTokenCreatedAt(LocalDateTime.now());
        token.setRealmId(realmId);

        tokenRepository.save(token);
        log.info("QuickBooks tokens saved. Company Realm ID: {}", realmId);
    }

    public ThirdPartyToken getValidTokenEntity() {
        ThirdPartyToken token = tokenRepository.findByProvider(PROVIDER_NAME)
                .orElseThrow(() -> new RuntimeException("QuickBooks not connected."));

        if (token.getTokenCreatedAt().plusSeconds(token.getExpiresInSeconds() - 300).isBefore(LocalDateTime.now())) {
            refreshToken(token);
        }
        return token;
    }

    public boolean isConnected() {
        return tokenRepository.findByProvider(PROVIDER_NAME).isPresent();
    }

    private void refreshToken(ThirdPartyToken token) {
        if (token.getRefreshToken() == null) throw new RuntimeException("No refresh token.");
        String encoding = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        WebClient webClient = getWebClient();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("refresh_token", token.getRefreshToken());
        formData.add("grant_type", "refresh_token");

        try {
            JsonNode response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Basic " + encoding)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("access_token")) {
                token.setAccessToken(response.get("access_token").asText());
                token.setExpiresInSeconds(response.get("expires_in").asLong());
                token.setTokenCreatedAt(LocalDateTime.now());
                if (response.has("refresh_token")) token.setRefreshToken(response.get("refresh_token").asText());
                tokenRepository.save(token);
            }
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new RuntimeException("Failed to refresh QuickBooks token.");
        }
    }

    // --- 2. BUSINESS LOGIC (SYNC) ---

    public void syncExpenseToQuickBooks(ExpenseRequest request) {
        ThirdPartyToken token = getValidTokenEntity();
        
        String cleanApiUrl = apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        String companyBaseUrl = cleanApiUrl + "/" + token.getRealmId();
        
        log.info(">>> QuickBooks Sync Target: {}", companyBaseUrl); 

        try {
            String vendorId = getOrCreateVendor(companyBaseUrl, token.getAccessToken(), request.getRequester());
            log.info(">>> Vendor ID found/created: {}", vendorId);

            createBill(companyBaseUrl, token.getAccessToken(), vendorId, request);
        } catch (Exception e) {
            log.error("Sync Logic Error", e);
            throw e;
        }
    }

    private String getOrCreateVendor(String baseUrl, String accessToken, User user) {
        WebClient client = getWebClient();
        String safeName = user.getFullName().replace("'", "\\'");
        
        // FIX: Xây dựng URL thủ công thay vì dùng uriBuilder phức tạp
        String query = "SELECT Id FROM Vendor WHERE DisplayName = '" + safeName + "'";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String fullQueryUrl = baseUrl + "/query?query=" + encodedQuery + "&minorversion=75";
        
        try {
            JsonNode queryResult = client.get()
                    .uri(URI.create(fullQueryUrl)) // Dùng URI.create để parse chuỗi URL đã chuẩn
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (queryResult != null && queryResult.has("QueryResponse") && queryResult.get("QueryResponse").has("Vendor")) {
                ArrayNode vendors = (ArrayNode) queryResult.get("QueryResponse").get("Vendor");
                if (vendors.size() > 0) {
                    return vendors.get(0).get("Id").asText();
                }
            }

            // Tạo Vendor mới
            ObjectNode vendorJson = objectMapper.createObjectNode();
            vendorJson.put("DisplayName", user.getFullName());
            ObjectNode primaryEmailAddr = objectMapper.createObjectNode();
            primaryEmailAddr.put("Address", user.getEmail());
            vendorJson.set("PrimaryEmailAddr", primaryEmailAddr);

            JsonNode createResult = client.post()
                    .uri(baseUrl + "/vendor?minorversion=75")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept", "application/json")
                    .bodyValue(vendorJson)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (createResult == null || !createResult.has("Vendor")) {
                 throw new RuntimeException("Failed to create vendor: Invalid response structure");
            }
            return createResult.get("Vendor").get("Id").asText();

        } catch (Exception e) {
            log.error("Error in getOrCreateVendor at URL: " + baseUrl, e);
            throw new RuntimeException("Vendor Sync Error: " + e.getMessage());
        }
    }

    private void createBill(String baseUrl, String accessToken, String vendorId, ExpenseRequest request) {
        WebClient client = getWebClient();
        try {
            ObjectNode billJson = objectMapper.createObjectNode();
            
            // 1. VendorRef
            billJson.putObject("VendorRef").put("value", vendorId);
            
            // 2. Line Items
            ArrayNode lines = billJson.putArray("Line");
            ObjectNode line = lines.addObject();
            
            line.put("DetailType", "AccountBasedExpenseLineDetail");
            line.put("Amount", request.getAmountTotal());
            
            ObjectNode detail = line.putObject("AccountBasedExpenseLineDetail");
            
            // Account Reference
            detail.putObject("AccountRef").put("value", defaultExpenseAccountId); 
            
            // CustomerRef (Optional)
            // detail.put("CustomerRef", objectMapper.createObjectNode().put("value", vendorId));

            // 3. Private Note
            billJson.put("PrivateNote", "Expense #" + request.getRequestNo() + ": " + request.getTitle());

            log.info("Sending Bill Payload: {}", billJson.toPrettyString());

            client.post()
                    .uri(baseUrl + "/bill?minorversion=75")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept", "application/json")
                    .bodyValue(billJson)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
                    
            log.info("Successfully created Bill in QuickBooks for Request {}", request.getRequestNo());
        } catch (WebClientResponseException e) {
            log.error("Error creating Bill. Status: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Bill Creation Error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error creating Bill", e);
            throw new RuntimeException("Bill Creation Error: " + e.getMessage());
        }
    }
}