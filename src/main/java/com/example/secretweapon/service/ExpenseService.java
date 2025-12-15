package com.example.secretweapon.service;



import com.example.secretweapon.exception.*;
import com.example.secretweapon.mapper.ExpenseMapper;
import com.example.secretweapon.model.dto.ExpenseValidationResult;
import com.example.secretweapon.model.dto.RuleDecision;
import com.example.secretweapon.model.entity.*;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.model.enums.HistoryAction;
import com.example.secretweapon.model.enums.Period;
import com.example.secretweapon.model.enums.RoleName;
import com.example.secretweapon.model.enums.RuleDecisionType;
import com.example.secretweapon.payload.request.ExpenseItemRequest;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.repository.ProjectRepository;
import com.example.secretweapon.rules.DynamicExpenseRule;
import com.google.api.services.storage.Storage.Projects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineParameters;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.secretweapon.model.enums.RuleDecisionType.ALLOW_NORMAL;
import static com.example.secretweapon.model.enums.RuleDecisionType.NEEDS_SPECIAL_APPROVAL;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRequestRepository expenseRepository;

    private final ExpenseMapper expenseMapper;

    //private final RequestHistoryService historyService;

    private final RuleService ruleService;

    private final ProjectService projectService;

    private final AnomalyDetectionService anomalyDetectionService;


    public List<ExpenseRequestResponse> getAllExpenseRequest(User admin) {
        boolean isAdmin = admin.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) throw new AccessDeniedException("You are not authorized to see this content");

        List<ExpenseRequestResponse> lisRequestResponses = expenseRepository.findAll().stream().map(expenseMapper::toResponse).toList();
        
        return lisRequestResponses;
    }

    public List<ExpenseRequestResponse> searchAllRequests(
            User currentUser, 
            ExpenseStatus status, 
            Long projectId, 
            Long requesterId,
            LocalDateTime startDate, 
            LocalDateTime endDate) {
        
        
        boolean canViewAll = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_FINANCE"));
        
        if (!canViewAll) {
            throw new AccessDeniedException("You are not authorized to search all requests");
        }

        List<ExpenseRequest> requests = expenseRepository.searchRequests(status, projectId, requesterId, startDate, endDate);
        return requests.stream().map(expenseMapper::toResponse).toList();
    }
    

    
    @Transactional(readOnly = true)
    public ExpenseRequestResponse getExpenseById(Long id, User currentUser) {
        ExpenseRequest request = findRequestById(id);

        boolean isOwner = request.getRequester().getId() == currentUser.getId();
        boolean isManager = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")) &&
                request.getRequester().getManager() != null &&
                request.getRequester().getManager().getId() == currentUser.getId();
        boolean isFinanceOrAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FINANCE") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isManager && !isFinanceOrAdmin) {
            throw new AccessDeniedException("You are not authorized to see this content");
        }

        return expenseMapper.toResponse(request);
    }

    private Integer countRequests(User requester, Period period) {
        Integer count = 0;

        LocalDateTime now = LocalDateTime.now();

        switch (period) {
            case DAY -> {
                LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
                count = expenseRepository.countByRequesterIdAndCreatedAtBetween(requester.getId(), startOfDay, now);
            }
            case WEEK -> {
                LocalDate today = now.toLocalDate();
                LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
                LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
                count = expenseRepository.countByRequesterIdAndCreatedAtBetween(requester.getId(), startOfWeekDateTime, now);
            }
            case MONTH -> {
                LocalDate startOfMonth = now.toLocalDate().withDayOfMonth(1);
                LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
                count = expenseRepository.countByRequesterIdAndCreatedAtBetween(requester.getId(), startOfMonthDateTime, now);
            }
        }

        return count;
    }

    public record UserDto(Long id, String name, String email) {}



    public RuleDecision evaluateRulesWithEasyRules(ExpenseRequestCreate request, User requester) {
        // 1. Lấy danh sách Rules (Cấu hình) từ DB
        List<Rule> candidateDbRules = ruleService.getMatchingRules(
                requester.getRole(),
                requester.getJobTitle(),
                request.getProjectId()
        );

        if (candidateDbRules.isEmpty()) {
            return new RuleDecision(RuleDecisionType.ALLOW_NORMAL, "No rules matched", null);
        }

        // 2. Chuẩn bị Engine
        // skipOnFirstAppliedRule(true): Nếu có 1 luật vi phạm -> Dừng lại luôn (Fail fast)
        RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstAppliedRule(true);
        RulesEngine rulesEngine = new DefaultRulesEngine(parameters);

        // 3. Chuẩn bị Facts (Dữ liệu đầu vào)
        Facts facts = new Facts();
        facts.put("request", request);      // Dữ liệu request
        facts.put("user", requester);       // Người dùng

        // Object để hứng kết quả
        ExpenseValidationResult validationResult = new ExpenseValidationResult();
        facts.put("result", validationResult);

        // 4. Đăng ký Rules vào Engine
        Rules rules = new Rules();
        for (Rule dbRule : candidateDbRules) {
            // Lấy số lượng request hiện tại để check frequency
            int currentCount = 0;
            if (dbRule.getLimitCountPerPeriod() != null) {
                 currentCount = countRequests(requester, dbRule.getPeriod());
            }
            
            // Convert Entity -> EasyRules Object
            rules.register(new DynamicExpenseRule(dbRule, currentCount));
        }

        // 5. Fire
        rulesEngine.fire(rules, facts);

        // 6. Kiểm tra kết quả trong validationResult
        if (!validationResult.isValid()) {
            return new RuleDecision(
                validationResult.getDecision(),
                validationResult.getReason(),
                validationResult.getViolatedRuleId()
            );
        }

        return new RuleDecision(RuleDecisionType.ALLOW_NORMAL, "Passed all rules", null);
    }

    @Transactional
    public ExpenseRequestResponse createExpense(ExpenseRequestCreate dto, User employee) {



        RuleDecision decision = evaluateRulesWithEasyRules(dto, employee);
        ExpenseRequest request = new ExpenseRequest();

        String attachmentUrl = null;
    if (dto.getItems() != null && !dto.getItems().isEmpty()) {
        log.info("Receipt URL: " + dto.getItems().get(0).getReceiptUrl());

        attachmentUrl = dto.getItems().get(0).getReceiptUrl(); 
    }

    Boolean isDuplicate = anomalyDetectionService.isReceiptDuplicate(attachmentUrl);

    log.info("Kết quả isDuplicate: " + isDuplicate);

    request.setIsAnomalous(isDuplicate); 

        if (decision.decision().equals(NEEDS_SPECIAL_APPROVAL) || isDuplicate) {
            request.setHasSpecialApproval(true); //if this draft is submited then it will display in a separate section in the manager's board.
            request.setSpecialApprovalReason(decision.reason());
        }
            request.setRequester(employee);
            request.setTitle(dto.getTitle());
            request.setDescription(dto.getDescription());
            request.setAttachments(dto.getAttachments());
            request.setMetadata(dto.getMetadata());
            request.setStatus(ExpenseStatus.DRAFT);
            request.setCurrency(dto.getCurrency());

            request.setRequestNo(generateRequestNo()); 

            Project project = projectService.getProjectById(dto.getProjectId());
            request.setProject(project);

            
            List<ExpenseItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            if (dto.getItems() != null) {
                for (ExpenseItemRequest itemDto : dto.getItems()) {
                    ExpenseItem item = expenseMapper.toItemEntity(itemDto);
                    item.setExpenseRequest(request);
                    items.add(item);
                    if (item.getAmount() != null) {
                        totalAmount = totalAmount.add(item.getAmount());
                    }
                }
            }
            request.setExpenseItems(items);
            request.setAmountTotal(totalAmount);

            request.addHistory(new RequestHistory(request, employee, HistoryAction.CREATED, "Created DRAFT"));

            ExpenseRequest savedRequest = expenseRepository.save(request);
            return expenseMapper.toResponse(savedRequest);

    }

        private String generateRequestNo() {
        // Ví dụ format: EXP-20231207-123456
        return "EXP-" + System.currentTimeMillis(); 
    }

    // Employee cập nhật request (khi là DRAFT) (EPIC 02)
    @Transactional
    public ExpenseRequestResponse updateExpense(Long id, ExpenseRequestCreate dto, User employee) {
        ExpenseRequest request = findRequestAndCheckOwnership(id, employee);

        if (request.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Can't update when expense is a draft");
        }
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setAttachments(dto.getAttachments());
        request.setMetadata(dto.getMetadata());
        request.setStatus(ExpenseStatus.DRAFT);
        request.setCurrency(dto.getCurrency());

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getItems() != null) {
            for (ExpenseItemRequest itemDto : dto.getItems()) {
                ExpenseItem item = expenseMapper.toItemEntity(itemDto);
                item.setExpenseRequest(request); 
                request.getExpenseItems().add(item);
                
                if (item.getAmount() != null) {
                    totalAmount = totalAmount.add(item.getAmount());
                }
            }
        }
        request.setAmountTotal(totalAmount);

        ExpenseRequest updatedRequest = expenseRepository.save(request);
        return expenseMapper.toResponse(updatedRequest);
    }

    @Transactional
    public void deleteExpense(Long id, User employee) {
        ExpenseRequest request = findRequestAndCheckOwnership(id, employee);

        if (request.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Can only delete request draft.");
        }
        expenseRepository.delete(request);
    }

    @Transactional
    public ExpenseRequestResponse submitExpense(Long id, User employee) {
        ExpenseRequest request = findRequestAndCheckOwnership(id, employee);

        if (request.getStatus() != ExpenseStatus.DRAFT) {
            throw new BadRequestException("Request đã được gửi hoặc đã xử lý");
        }

        request.setStatus(ExpenseStatus.PENDING_MANAGER);
        request.addHistory(new RequestHistory(request, employee, HistoryAction.SUBMITTED, "Gửi duyệt cho Manager"));

        ExpenseRequest savedRequest = expenseRepository.save(request);
        return expenseMapper.toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ExpenseRequestResponse> getMyExpenses(User employee) {
        return expenseRepository.findByRequesterOrderByCreatedAtDesc(employee).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }


    private ExpenseRequest findRequestById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy request với ID: " + id));
    }

    private ExpenseRequest findRequestAndCheckOwnership(Long id, User employee) {
        ExpenseRequest request = findRequestById(id);
        if (request.getRequester().getId() != employee.getId()) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa request này");
        }
        return request;
    }
}