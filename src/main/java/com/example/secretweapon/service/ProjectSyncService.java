package com.example.secretweapon.service;

import com.example.secretweapon.service.client.JiraClient;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.model.dto.JiraProjectDto;
import com.example.secretweapon.model.dto.JiraUserDto;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ProjectStatus;
import com.example.secretweapon.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectSyncService {

    private final JiraClient jiraClient;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectSyncService(JiraClient jiraClient, ProjectRepository projectRepository, UserService userService) {
        this.jiraClient = jiraClient;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Transactional
    public void syncProjects() {
        List<JiraProjectDto> jiraProjects = jiraClient.getAllProjects(); //blocking
        /*mẫu response 
        {
    "expand":"description,lead,issueTypes,url,projectKeys,permissions,insight",
    "self":"https://bich-quyen-27.atlassian.net/rest/api/3/project/10000",
    "id":"10000",
    "key":"SCRUM",
    "name":"My Scrum Space",
    "avatarUrls":{...},
    "projectTypeKey":"software",
    "simplified":true,
    "style":"next-gen",
    "isPrivate":false,
    "properties":{},
    "entityId":"...",
    "uuid":"..."
}

        */


        for (JiraProjectDto jp : jiraProjects) {

            Optional<Project> existingProjectOpt = projectRepository.findByJiraKey(jp.getKey());

            Project project;
            if (existingProjectOpt.isPresent()) {
                project = existingProjectOpt.get();
            } else {
                project = new Project();
                project.setJiraKey(jp.getKey());
                project.setBudgetUsed(BigDecimal.ZERO);
            }

            project.setName(jp.getName());
            project.setMetadata(jp.toJson());
            project.setStatus(ProjectStatus.ACTIVE);

            JiraUserDto leadFullInfo = jiraClient.getLeadDetailByProject(jp.getKey());

            

            if (jiraClient.getProjectCreationTime(jp.getKey()) != null) {
                LocalDateTime createdTimestamp = jiraClient.getProjectCreationTime(jp.getKey()).toLocalDateTime();
                project.setCreatedAt(createdTimestamp);                       

            }

            if (jiraClient.getProjectLastUpdatedTime(jp.getKey()) != null)
                {
                    LocalDateTime lastUpdatedTimestamp = jiraClient.getProjectLastUpdatedTime(jp.getKey()).toLocalDateTime();
                    project.setUpdatedAt(lastUpdatedTimestamp);
                }
            

            
                        

        if (leadFullInfo != null && leadFullInfo.getEmailAddress() != null) {
            String email = leadFullInfo.getEmailAddress();
            User manager = userService.getUserByEmailAddress(email);
            if (manager != null) {
                project.setManager(manager);
            }
        }
        
        projectRepository.save(project);
        }
    }
}