package com.example.secretweapon.controller;

import com.example.secretweapon.payload.request.RuleRequest;
import com.example.secretweapon.payload.response.RuleResponse;
import com.example.secretweapon.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RuleResponse>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RuleResponse> createRule(@RequestBody RuleRequest request) {
        return ResponseEntity.ok(ruleService.createRule(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleRule(@PathVariable Long id) {
        ruleService.toggleRule(id);
        return ResponseEntity.ok().build();
    }
}