package com.example.secretweapon.service;

import org.springframework.stereotype.Service;

import com.example.secretweapon.repository.ProjectBudgetHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectBudgetHistoryService {
    private final ProjectBudgetHistoryRepository projectBudgetHistoryRepository;
    
}
