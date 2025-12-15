package com.example.secretweapon.service.impl;


import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.RuleMapper;
import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.payload.request.RuleRequest;
import com.example.secretweapon.payload.response.RuleResponse;
import com.example.secretweapon.repository.RuleRepository;
import com.example.secretweapon.service.ProjectService;
import com.example.secretweapon.service.RoleService;
import com.example.secretweapon.service.RuleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final RuleRepository ruleRepository;
    private final RoleService roleService;
    private final ProjectService projectService;
    private final RuleMapper ruleMapper;

    @Override
    public List<Rule> getMatchingRules(Role role, JobTitle jobTitle, Long projectId) {
        return ruleRepository.findMatchingRules(role, jobTitle, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(ruleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        Rule rule = ruleMapper.toEntity(request);
        rule.setEnabled(true);
        rule.setJobTitle(request.getJobTitle());
        rule.setLimitAmount(BigDecimal.valueOf(request.getLimitAmount()));
        rule.setLimitCountPerPeriod(request.getLimitAmountPerPeriod());
        rule.setName(request.getName());
        rule.setPeriod(request.getPeriod());
        rule.setPriority(request.getPriority());
        if (request.getRoleId() != null) {
            Role role = roleService.getRoleById(request.getRoleId()).orElseThrow(
                () -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId())
            );
            rule.setRole(role);
        }

        if (request.getProjectId() != null) {
            Project project = projectService.getProjectById(request.getProjectId());
            rule.setProject(project);
        }
        
        // Business logic: Nếu không có priority, set mặc định là thấp nhất (ví dụ 999) hoặc cao nhất tùy logic
        if (rule.getPriority() == null) {
            rule.setPriority(10);
        }
        
        Rule savedRule = ruleRepository.save(rule);
        return ruleMapper.toResponse(savedRule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rule not found with id: " + id);
        }
        ruleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleRule(Long id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + id));
        
        // Business logic: Đảo ngược trạng thái
        rule.setEnabled(!Boolean.TRUE.equals(rule.getEnabled()));
        ruleRepository.save(rule);
    }
}