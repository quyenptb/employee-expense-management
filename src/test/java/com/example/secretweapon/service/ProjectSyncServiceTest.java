package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.JiraProjectDto;
import com.example.secretweapon.model.dto.JiraUserDto;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.repository.ProjectRepository;
import com.example.secretweapon.service.ProjectSyncService;
import com.example.secretweapon.service.UserService;
import com.example.secretweapon.service.client.JiraClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectSyncServiceTest {

    @Mock
    private JiraClient jiraClient;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectSyncService projectSyncService;

    @Test
    void syncProjects_CreateNew() {
        JiraProjectDto dto = new JiraProjectDto();
        dto.setKey("SWP");
        dto.setName("Secret Weapon");
        JiraUserDto lead = new JiraUserDto();
        lead.setEmailAddress("lead@example.com");
        dto.setLead(lead);
        

        List<JiraProjectDto> jiraList = List.of(dto);

        when(jiraClient.getAllProjects()).thenReturn(jiraList);
        //when(projectRepository.findByJiraKey("SWP")).thenReturn(Optional.empty());
        //when(userService.getUserByEmailAddress("lead@example.com")).thenReturn(new User());
        when(jiraClient.getProjectCreationTime(dto.getKey())).thenReturn(ZonedDateTime.now());
        when(jiraClient.getProjectLastUpdatedTime(dto.getKey())).thenReturn(ZonedDateTime.now());
        
        projectSyncService.syncProjects();

        verify(projectRepository).save(argThat(proj ->
                proj.getJiraKey().equals("SWP") &&
                        proj.getName().equals("Secret Weapon") &&
                        proj.getBudgetUsed().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    void syncProjects_UpdateExisting() {
        JiraProjectDto dto = new JiraProjectDto();
        dto.setKey("SWP");
        dto.setName("Secret Weapon New Name");
        dto.setLead(new JiraUserDto());

        Project existingProject = new Project();
        existingProject.setJiraKey("SWP");
        existingProject.setName("Old Name");
        existingProject.setBudgetUsed(new BigDecimal("1000"));
        


        when(jiraClient.getAllProjects()).thenReturn(List.of(dto));
        when(jiraClient.getProjectCreationTime(dto.getKey())).thenReturn(ZonedDateTime.now());
        when(jiraClient.getProjectLastUpdatedTime(dto.getKey())).thenReturn(ZonedDateTime.now());
        when(projectRepository.findByJiraKey("SWP")).thenReturn(Optional.of(existingProject));

        projectSyncService.syncProjects();

        verify(projectRepository).save(argThat(proj ->
                proj.getName().equals("Secret Weapon New Name") &&
                        proj.getBudgetUsed().equals(new BigDecimal("1000"))
        ));
    }
}