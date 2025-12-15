package com.example.secretweapon.repository;

import com.example.secretweapon.model.entity.*;
import com.example.secretweapon.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExpenseRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRequestRepository expenseRequestRepository;

    @Test
    @DisplayName("searchRequests_FilterByStatusAndProject_shouldReturnMatchingRequests")
    void searchRequests_FilterByStatusAndProject_shouldReturnMatchingRequests() {
        // Arrange
        Project project1 = new Project();
        project1.setName("Project A");
        entityManager.persist(project1);

        Project project2 = new Project();
        project2.setName("Project B");
        entityManager.persist(project2);

        Role role = new Role();
        role.setName(RoleName.ROLE_EMPLOYEE);
        entityManager.persist(role);

        User user = new User();
        user.setEmail("user@test.com");
        user.setFullName("Test User");
        user.setPassword("pass");
        user.setRole(role);
        entityManager.persist(user);

        ExpenseRequest req1 = new ExpenseRequest();
        req1.setRequestNo("REQ-001");
        req1.setProject(project1);
        req1.setRequester(user);
        req1.setStatus(ExpenseStatus.DRAFT);
        req1.setAmountTotal(BigDecimal.TEN);
        req1.setTitle("Req 1");
        req1.setCreatedAt(LocalDateTime.now());
        entityManager.persist(req1);

        ExpenseRequest req2 = new ExpenseRequest();
        req2.setRequestNo("REQ-002");
        req2.setProject(project2);
        req2.setRequester(user);
        req2.setStatus(ExpenseStatus.APPROVED);
        req2.setAmountTotal(BigDecimal.TEN);
        req2.setTitle("Req 2");
        req2.setCreatedAt(LocalDateTime.now());
        entityManager.persist(req2);

        // Act
        List<ExpenseRequest> results = expenseRequestRepository.searchRequests(
                ExpenseStatus.DRAFT,
                project1.getId(),
                null,
                null,
                null
        );

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRequestNo()).isEqualTo("REQ-001");
    }

    @Test
    @DisplayName("countByRequesterIdAndCreatedAtBetween_DateRange_shouldReturnCorrectCount")
    void countByRequesterIdAndCreatedAtBetween_DateRange_shouldReturnCorrectCount() {
        // Arrange
        Role role = new Role();
        role.setName(RoleName.ROLE_EMPLOYEE);
        entityManager.persist(role);

        User user = new User();
        user.setEmail("count@test.com");
        user.setFullName("Counter");
        user.setPassword("pass");
        user.setRole(role);
        entityManager.persist(user);

        Project project = new Project();
        project.setName("Proj Count");
        entityManager.persist(project);

        ExpenseRequest reqOld = new ExpenseRequest();
        reqOld.setRequestNo("REQ-OLD");
        reqOld.setRequester(user);
        reqOld.setProject(project);
        reqOld.setAmountTotal(BigDecimal.TEN);
        reqOld.setStatus(ExpenseStatus.DRAFT);
        reqOld.setTitle("Old");
        reqOld.setCreatedAt(LocalDateTime.now().minusDays(10));
        entityManager.persist(reqOld);

        ExpenseRequest reqNew = new ExpenseRequest();
        reqNew.setRequestNo("REQ-NEW");
        reqNew.setRequester(user);
        reqNew.setProject(project);
        reqNew.setAmountTotal(BigDecimal.TEN);
        reqNew.setStatus(ExpenseStatus.DRAFT);
        reqNew.setTitle("New");
        reqNew.setCreatedAt(LocalDateTime.now());
        entityManager.persist(reqNew);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // Act
        Integer count = expenseRequestRepository.countByRequesterIdAndCreatedAtBetween(
                user.getId(), start, end
        );

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("findAverageAmountTotalByRequesterAndUpdatedAtBetween_ValidData_shouldReturnAverage")
    void findAverageAmountTotalByRequesterAndUpdatedAtBetween_ValidData_shouldReturnAverage() {
        // Arrange
        Role role = new Role();
        role.setName(RoleName.ROLE_EMPLOYEE);
        entityManager.persist(role);

        User user = new User();
        user.setEmail("avg@test.com");
        user.setFullName("Avg User");
        user.setPassword("pass");
        user.setRole(role);
        entityManager.persist(user);

        Project project = new Project();
        project.setName("Proj Avg");
        entityManager.persist(project);

        createAndPersistRequest(user, project, new BigDecimal("100"), LocalDateTime.now());
        createAndPersistRequest(user, project, new BigDecimal("200"), LocalDateTime.now());

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // Act
        BigDecimal average = expenseRequestRepository.findAverageAmountTotalByRequesterAndUpdatedAtBetween(
                user.getId(), start, end
        );

        // Assert
        assertThat(average).isEqualByComparingTo(new BigDecimal("150"));
    }

    @Test
    @DisplayName("findHistoryByManager_ManagerHasPendingRequests_shouldReturnRequests")
    void findHistoryByManager_ManagerHasPendingRequests_shouldReturnRequests() {
        // Arrange
        Role roleManager = new Role();
        roleManager.setName(RoleName.ROLE_MANAGER);
        entityManager.persist(roleManager);

        Role roleEmployee = new Role();
        roleEmployee.setName(RoleName.ROLE_EMPLOYEE);
        entityManager.persist(roleEmployee);

        User manager = new User();
        manager.setEmail("mgr@test.com");
        manager.setFullName("Manager");
        manager.setPassword("pass");
        manager.setRole(roleManager);
        entityManager.persist(manager);

        User employee = new User();
        employee.setEmail("emp@test.com");
        employee.setFullName("Employee");
        employee.setPassword("pass");
        employee.setRole(roleEmployee);
        employee.setManager(manager);
        entityManager.persist(employee);

        Project project = new Project();
        project.setName("Proj Hist");
        entityManager.persist(project);

        ExpenseRequest req = new ExpenseRequest();
        req.setRequestNo("REQ-HIST");
        req.setRequester(employee);
        req.setProject(project);
        req.setAmountTotal(BigDecimal.TEN);
        req.setStatus(ExpenseStatus.PENDING_FINANCE); 
        req.setTitle("History Req");
        req.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(req);

        // Act
        List<ExpenseRequest> history = expenseRequestRepository.findHistoryByManager(manager.getId());

        // Assert
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getRequestNo()).isEqualTo("REQ-HIST");
    }

    private void createAndPersistRequest(User user, Project project, BigDecimal amount, LocalDateTime time) {
        ExpenseRequest req = new ExpenseRequest();
        req.setRequestNo("REQ-" + amount);
        req.setRequester(user);
        req.setProject(project);
        req.setAmountTotal(amount);
        req.setStatus(ExpenseStatus.APPROVED);
        req.setTitle("Req " + amount);
        req.setUpdatedAt(time); 
        req.setCreatedAt(time);
        entityManager.persist(req);
    }
}