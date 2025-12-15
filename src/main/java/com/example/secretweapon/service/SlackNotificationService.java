package com.example.secretweapon.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
public class SlackNotificationService {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendNotification(String message) {
        Map<String, Object> slackMessage = new HashMap<>();
        slackMessage.put("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(slackMessage, headers);

        try {
            restTemplate.postForEntity(slackWebhookUrl, request, String.class);
            System.out.println("Send notification successfully!");
            
        } catch (Exception e) {
            System.err.println("Error when send Slack notification: " + e.getMessage());
            
        }
    }
}