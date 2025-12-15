package com.example.secretweapon.service.client;

import com.example.secretweapon.model.dto.JiraProjectDto;
import com.example.secretweapon.model.dto.JiraUserDto;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.time.format.*;

@Service
@Slf4j
public class JiraClient {
    private final WebClient webClient;
    private static final DateTimeFormatter JIRA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    


    public JiraClient(@Value("${jira.base-url}") String baseUrl,
                      @Value("${jira.email}") String email,
                      @Value("${jira.api-token}") String apiToken) {
        String auth = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes());
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .build();
    }

    public List<JiraProjectDto> getAllProjects() {
        return webClient.get()
                .uri("/rest/api/3/project")
                .retrieve()
                .bodyToFlux(JiraProjectDto.class)
                .collectList()
                .block();
    }


    public JiraUserDto getLeadDetailByProject(String projectKeyOrId) {
        JiraProjectDto projectDto = webClient.get()
                .uri("/rest/api/3/project/{id}?expand=lead", projectKeyOrId)
                .retrieve()
                .bodyToMono(JiraProjectDto.class)
                .block();

        if (projectDto == null || projectDto.getLead() == null || projectDto.getLead().getAccountId() == null) {
            return null;
        }

        String accountId = projectDto.getLead().getAccountId();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/3/user")
                        .queryParam("accountId", accountId)
                        .build())
                .retrieve()
                .bodyToMono(JiraUserDto.class)
                .block();
    }

public ZonedDateTime getProjectCreationTime(String projectKeyOrId) {
        log.info("Fetching creation time for project: {}", projectKeyOrId); // Log request start

        String jql = String.format("project = \"%s\" ORDER BY created ASC", projectKeyOrId);
        
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search/jql") 
                        .queryParam("jql", jql)
                        .queryParam("maxResults", 1)
                        .queryParam("fields", "created")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("issues")) {
            JsonNode issues = response.get("issues");
            if (issues.isArray() && issues.size() > 0) {
                JsonNode firstIssue = issues.get(0);
                JsonNode createdField = firstIssue.path("fields").path("created");
                
                if (!createdField.isMissingNode()) {
                    String timestampString = createdField.asText();
                    ZonedDateTime createdTime = ZonedDateTime.parse(timestampString, JIRA_DATE_FORMATTER);
                    log.debug("Found creation time for {}: {}", projectKeyOrId, createdTime); 
                    return createdTime;
                }
            }
        }
        
        log.warn("Could not find creation time for project: {}", projectKeyOrId); 
        return null; 
    }


    public ZonedDateTime getProjectLastUpdatedTime(String projectKeyOrId) {
        String jql = String.format("project = \"%s\" ORDER BY updated DESC", projectKeyOrId);
        
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search/jql") 
                        .queryParam("jql", jql)
                        .queryParam("maxResults", 1)
                        .queryParam("fields", "updated") // Request field 'updated'
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("issues")) {
            JsonNode issues = response.get("issues");
            if (issues.isArray() && issues.size() > 0) {
                JsonNode firstIssue = issues.get(0);
                
                JsonNode updatedField = firstIssue.path("fields").path("updated"); 
                
                if (!updatedField.isMissingNode()) {
                        log.info(updatedField.toString());
                    String timestampString = updatedField.asText();
                                        log.debug("Found creation time for {}: {}", projectKeyOrId, updatedField); 

                    return ZonedDateTime.parse(timestampString, JIRA_DATE_FORMATTER);
                }
            }
        }
        return null;
    }

}