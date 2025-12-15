package com.example.secretweapon.controller;

import com.example.secretweapon.payload.request.RuleRequest;
import com.example.secretweapon.payload.response.RuleResponse;
import com.example.secretweapon.service.RuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RuleService ruleService;

    @InjectMocks
    private RuleController ruleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ruleController).build();
    }

    @Test
    @DisplayName("getAllRules_shouldReturnList")
    void getAllRules_shouldReturnList() throws Exception {
        RuleResponse rule = RuleResponse.builder().id(1L).name("Rule 1").build();
        when(ruleService.getAllRules()).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rule 1"));
    }

    @Test
    @DisplayName("createRule_Valid_shouldReturnRule")
    void createRule_Valid_shouldReturnRule() throws Exception {
        RuleRequest req = new RuleRequest();
        req.setName("Limit 50M");
        
        RuleResponse res = RuleResponse.builder().id(1L).name("Limit 50M").build();

        when(ruleService.createRule(any(RuleRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Limit 50M"));
    }

    @Test
    @DisplayName("toggleRule_Valid_shouldReturnOk")
    void toggleRule_Valid_shouldReturnOk() throws Exception {
        Long id = 1L;
        doNothing().when(ruleService).toggleRule(id);

        mockMvc.perform(patch("/api/rules/{id}/toggle", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deleteRule_Valid_shouldReturnNoContent")
    void deleteRule_Valid_shouldReturnNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(ruleService).deleteRule(id);

        mockMvc.perform(delete("/api/rules/{id}", id))
                .andExpect(status().isNoContent());
    }
}