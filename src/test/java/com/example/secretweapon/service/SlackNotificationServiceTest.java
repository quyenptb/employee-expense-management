package com.example.secretweapon.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlackNotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SlackNotificationService slackNotificationService;

    @BeforeEach
    void setUp() {
        // Inject url
        ReflectionTestUtils.setField(slackNotificationService, "slackWebhookUrl", "https://hooks.slack.com/services/xxx");
        // Inject mock RestTemplate vào field private (vì code gốc dùng new RestTemplate())
        ReflectionTestUtils.setField(slackNotificationService, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("sendNotification_Success_shouldPostToSlack")
    void sendNotification_Success_shouldPostToSlack() {
        // Arrange
        String message = "Test Message";
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(null);

        // Act
        slackNotificationService.sendNotification(message);

        // Assert
        verify(restTemplate).postForEntity(
                eq("https://hooks.slack.com/services/xxx"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("sendNotification_Exception_shouldCatchAndLog")
    void sendNotification_Exception_shouldCatchAndLog() {
        // Arrange
        String message = "Fail Message";
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Slack down"));

        // Act
        // Hàm này có try-catch nên test sẽ không fail
        slackNotificationService.sendNotification(message);

        // Assert
        verify(restTemplate).postForEntity(anyString(), any(), any());
    }
}