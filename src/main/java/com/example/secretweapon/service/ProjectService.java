package com.example.secretweapon.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.ProjectMapper;
import com.example.secretweapon.mapper.UserMapper;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.payload.request.ProjectUpdateBudgetRequest;
import com.example.secretweapon.payload.response.ProjectResponse;
import com.example.secretweapon.repository.ProjectRepository;
import com.example.secretweapon.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper; 
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Project theo Id"));
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProjectDetails(Long id, ProjectUpdateBudgetRequest request) {
        Project project = getProjectById(id);

        if (request.getBudgetTotal() != null) {
            project.setBudgetTotal(request.getBudgetTotal());
        }

    

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            project.setManager(manager);
        }

        return mapToResponse(projectRepository.save(project));
    }

    private ProjectResponse mapToResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .jiraKey(p.getJiraKey())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .budgetTotal(p.getBudgetTotal())
                .budgetUsed(p.getBudgetUsed())
                .status(p.getStatus())
                .metadata(p.getMetadata())
                .manager(p.getManager() != null ? userMapper.toSummary(p.getManager()) : null)
                .build();
    }
    
    
}
