package com.example.secretweapon.repository;

import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.Role;
import com.example.secretweapon.model.entity.Rule;
import com.example.secretweapon.model.enums.JobTitle;
import com.example.secretweapon.model.enums.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RuleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RuleRepository ruleRepository;

    @Test
    @DisplayName("findMatchingRules_MatchingRoleAndProject_shouldReturnRules")
    void findMatchingRules_MatchingRoleAndProject_shouldReturnRules() {
        // Arrange
        Role roleEmployee = new Role();
        roleEmployee.setName(RoleName.ROLE_EMPLOYEE);
        entityManager.persist(roleEmployee);

        Project project = new Project();
        project.setName("Test Project");
        entityManager.persist(project);

        Rule ruleGeneral = new Rule();
        ruleGeneral.setName("General Rule");
        ruleGeneral.setEnabled(true);
        ruleGeneral.setPriority(1);
        entityManager.persist(ruleGeneral);

        Rule ruleRoleSpecific = new Rule();
        ruleRoleSpecific.setName("Role Specific Rule");
        ruleRoleSpecific.setRole(roleEmployee);
        ruleRoleSpecific.setEnabled(true);
        ruleRoleSpecific.setPriority(2);
        entityManager.persist(ruleRoleSpecific);

        Rule ruleProjectSpecific = new Rule();
        ruleProjectSpecific.setName("Project Specific Rule");
        ruleProjectSpecific.setProject(project);
        ruleProjectSpecific.setEnabled(true);
        ruleProjectSpecific.setPriority(3);
        entityManager.persist(ruleProjectSpecific);

        Rule ruleDisabled = new Rule();
        ruleDisabled.setName("Disabled Rule");
        ruleDisabled.setEnabled(false);
        entityManager.persist(ruleDisabled);

        // Act
        List<Rule> matches = ruleRepository.findMatchingRules(
                roleEmployee,
                null,
                project.getId()
        );

        // Assert
        assertThat(matches).hasSize(3); 
        assertThat(matches).extracting(Rule::getName)
                .containsExactlyInAnyOrder("General Rule", "Role Specific Rule", "Project Specific Rule");
    }

    @Test
    @DisplayName("findMatchingRules_JobTitleSpecific_shouldReturnMatchingRule")
    void findMatchingRules_JobTitleSpecific_shouldReturnMatchingRule() {
        // Arrange
        Role roleEmployee = new Role();
        roleEmployee.setName(RoleName.ROLE_EMPLOYEE);
        entityManager.persist(roleEmployee);

        Rule ruleDev = new Rule();
        ruleDev.setName("Dev Rule");
        ruleDev.setJobTitle(JobTitle.DEV);
        ruleDev.setEnabled(true);
        ruleDev.setPriority(1);
        entityManager.persist(ruleDev);

        Rule ruleBa = new Rule();
        ruleBa.setName("BA Rule");
        ruleBa.setJobTitle(JobTitle.BA);
        ruleBa.setEnabled(true);
        ruleBa.setPriority(1);
        entityManager.persist(ruleBa);

        // Act
        List<Rule> matches = ruleRepository.findMatchingRules(
                roleEmployee,
                JobTitle.DEV,
                null
        );

        // Assert
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getName()).isEqualTo("Dev Rule");
    }
}