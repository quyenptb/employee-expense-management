package com.example.secretweapon.service;


import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.payload.request.RuleRequest;
import com.example.secretweapon.payload.response.RuleResponse;

import java.util.List;

public interface RuleService {

    List<Rule> getMatchingRules(Role role, JobTitle jobTitle, Long projectId);

    List<RuleResponse> getAllRules();
    
    RuleResponse createRule(RuleRequest request);
    
    void deleteRule(Long id);
    
    void toggleRule(Long id);
}