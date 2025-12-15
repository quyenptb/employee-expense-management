package com.example.secretweapon.controller;

import com.example.secretweapon.model.dto.ProjectHealthDTO;
import com.example.secretweapon.service.ProjectAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ProjectAnalyticsService analyticsService;


    @GetMapping("/project/{id}/forecast")
    public ResponseEntity<ProjectAnalyticsService.ProjectAnalyticsDTO> getProjectForecast(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getProjectForecast(id));
    }

    @GetMapping("/project/{id}/health")
    public ResponseEntity<ProjectHealthDTO> getProjectHealth(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getProjectHealth(id));
    }
}