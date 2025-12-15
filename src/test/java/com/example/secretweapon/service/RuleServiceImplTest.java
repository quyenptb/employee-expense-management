package com.example.secretweapon.service;

import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.model.enums.Period;
import com.example.secretweapon.payload.request.RuleRequest;
import com.example.secretweapon.payload.response.RuleResponse;
import com.example.secretweapon.repository.RuleRepository;
import com.example.secretweapon.service.ProjectService;
import com.example.secretweapon.service.RoleService;
import com.example.secretweapon.service.impl.RuleServiceImpl;
import com.example.secretweapon.mapper.RuleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceImplTest {

    @Mock
    private RuleRepository ruleRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private ProjectService projectService;
    @Mock
    private RuleMapper ruleMapper;

    @InjectMocks
    private RuleServiceImpl ruleService;

    @Test
    @DisplayName("createRule_ValidRequest_shouldReturnRuleResponse")
    void createRule_ValidRequest_shouldReturnRuleResponse() {
        // Arrange
        RuleRequest request = new RuleRequest();
        request.setName("Test Rule");
        request.setLimitAmount(1000);
        request.setLimitAmountPerPeriod(5);
        request.setPeriod(Period.MONTH);
        request.setRoleId(1L);
        request.setProjectId(2L);
        request.setPriority(1);

        Rule ruleEntity = new Rule();
        ruleEntity.setId(1L);

        when(ruleMapper.toEntity(request)).thenReturn(ruleEntity);
        when(roleService.getRoleById(1L)).thenReturn(Optional.of(new Role()));
        when(projectService.getProjectById(2L)).thenReturn(new Project());
        when(ruleRepository.save(any(Rule.class))).thenReturn(ruleEntity);
        when(ruleMapper.toResponse(any())).thenReturn(new RuleResponse());

        // Act
        RuleResponse response = ruleService.createRule(request);

        // Assert
        assertNotNull(response);
        verify(ruleRepository).save(ruleEntity);
        assertTrue(ruleEntity.getEnabled());
    }

    @Test
    @DisplayName("toggleRule_ExistingId_shouldToggleStatus")
    void toggleRule_ExistingId_shouldToggleStatus() {
        // Arrange
        Long ruleId = 1L;
        Rule rule = new Rule();
        rule.setId(ruleId);
        rule.setEnabled(true);

        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        // Act
        ruleService.toggleRule(ruleId);

        // Assert
        assertFalse(rule.getEnabled());
        verify(ruleRepository).save(rule);
    }

    @Test
    @DisplayName("deleteRule_ExistingId_shouldDeleteRule")
    void deleteRule_ExistingId_shouldDeleteRule() {
        // Arrange
        Long ruleId = 1L;
        when(ruleRepository.existsById(ruleId)).thenReturn(true);

        // Act
        ruleService.deleteRule(ruleId);

        // Assert
        verify(ruleRepository).deleteById(ruleId);
    }
}