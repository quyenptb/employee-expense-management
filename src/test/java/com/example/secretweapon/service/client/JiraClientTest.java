package com.example.secretweapon.service.client;

import com.example.secretweapon.model.dto.JiraProjectDto;
import com.example.secretweapon.model.dto.JiraUserDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JiraClientTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private JiraClient jiraClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jiraClient, "webClient", webClient);
    }

    private void mockWebClientGetChain() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private void mockWebClientGetChainWithUriFunction() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("getAllProjects_shouldReturnList")
    void getAllProjects_shouldReturnList() {
        mockWebClientGetChain();
        JiraProjectDto dto = new JiraProjectDto();
        dto.setKey("SWP");
        when(responseSpec.bodyToFlux(JiraProjectDto.class)).thenReturn(Flux.just(dto));

        List<JiraProjectDto> result = jiraClient.getAllProjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SWP", result.get(0).getKey());
    }

    @Test
    @DisplayName("getLeadDetailByProject_ValidResponse_shouldReturnUser")
    void getLeadDetailByProject_ValidResponse_shouldReturnUser() {
        // Setup call 1 (Get Project)
        WebClient.RequestHeadersUriSpec uriSpec1 = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec1 = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec1 = mock(WebClient.ResponseSpec.class);

        // Setup call 2 (Get User)
        WebClient.RequestHeadersUriSpec uriSpec2 = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec2 = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec2 = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpec1).thenReturn(uriSpec2);

        // Mock Call 1
        when(uriSpec1.uri(eq("/rest/api/3/project/{id}?expand=lead"), anyString())).thenReturn(headersSpec1);
        when(headersSpec1.retrieve()).thenReturn(responseSpec1);
        JiraProjectDto projectDto = new JiraProjectDto();
        JiraUserDto leadSimple = new JiraUserDto();
        leadSimple.setAccountId("ACC-123");
        projectDto.setLead(leadSimple);
        when(responseSpec1.bodyToMono(JiraProjectDto.class)).thenReturn(Mono.just(projectDto));

        // Mock Call 2
        when(uriSpec2.uri(any(Function.class))).thenReturn(headersSpec2);
        when(headersSpec2.retrieve()).thenReturn(responseSpec2);
        JiraUserDto leadFull = new JiraUserDto();
        leadFull.setEmailAddress("lead@test.com");
        when(responseSpec2.bodyToMono(JiraUserDto.class)).thenReturn(Mono.just(leadFull));

        JiraUserDto result = jiraClient.getLeadDetailByProject("SWP");

        assertNotNull(result);
        assertEquals("lead@test.com", result.getEmailAddress());
    }

    @Test
    @DisplayName("getProjectCreationTime_FoundIssue_shouldReturnTime")
    void getProjectCreationTime_FoundIssue_shouldReturnTime() throws Exception {
        mockWebClientGetChainWithUriFunction();

        String json = """
            {
                "issues": [
                    {
                        "fields": {
                            "created": "2023-10-01T12:00:00.000+0700"
                        }
                    }
                ]
            }
        """;
        JsonNode node = objectMapper.readTree(json);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(node));

        ZonedDateTime result = jiraClient.getProjectCreationTime("SWP");

        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(10, result.getMonthValue());
    }

    @Test
    @DisplayName("getProjectLastUpdatedTime_FoundIssue_shouldReturnTime")
    void getProjectLastUpdatedTime_FoundIssue_shouldReturnTime() throws Exception {
        mockWebClientGetChainWithUriFunction();

        String json = """
            {
                "issues": [
                    {
                        "fields": {
                            "updated": "2023-12-15T15:30:00.000+0700"
                        }
                    }
                ]
            }
        """;
        JsonNode node = objectMapper.readTree(json);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(node));

        ZonedDateTime result = jiraClient.getProjectLastUpdatedTime("SWP");

        assertNotNull(result);
        assertEquals(15, result.getDayOfMonth());
        assertEquals(15, result.getHour());
    }
    
    @Test
    @DisplayName("getProjectCreationTime_NoIssues_shouldReturnNull")
    void getProjectCreationTime_NoIssues_shouldReturnNull() throws Exception {
        mockWebClientGetChainWithUriFunction();

        String json = "{ \"issues\": [] }";
        JsonNode node = objectMapper.readTree(json);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(node));

        ZonedDateTime result = jiraClient.getProjectCreationTime("SWP");

        assertNull(result);
    }
}