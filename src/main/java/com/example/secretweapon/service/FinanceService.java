package com.example.secretweapon.service;


import com.example.secretweapon.exception.BadRequestException;
import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.mapper.ExpenseMapper;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.entity.ProjectBudgetHistory;
import com.example.secretweapon.model.entity.RequestHistory;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.model.enums.HistoryAction;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.repository.ProjectBudgetHistoryRepository;
import com.example.secretweapon.repository.ProjectRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FinanceService {
    private final ExpenseRequestRepository expenseRepository;

    private final ExpenseMapper expenseMapper;

    private final ProjectService projectService;

    private final ProjectBudgetHistoryRepository projectBudgetHistoryRepository;

    private final AccountingIntegrationService accountingIntegrationService;

    // Lấy các request đang chờ Finance duyệt (EPIC 04)
    public List<ExpenseRequestResponse> getPendingFinanceRequests() {
        return expenseRepository.findByStatusInOrderByUpdatedAtDesc(
                List.of(ExpenseStatus.PENDING_FINANCE)
        ).stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    // Finance duyệt (final approve) (EPIC 04)
    @Transactional
    public ExpenseRequestResponse approveFinanceRequest(Long requestId, User financeUser, ApprovalRequest approvalRequest) {
    
        ExpenseRequest request = findRequestByIdAndCheckStatus(requestId, ExpenseStatus.PENDING_FINANCE);

    
        

        request.setStatus(ExpenseStatus.APPROVED); // Chờ thanh toán
        request.addHistory(new RequestHistory(
                request,
                financeUser,
                HistoryAction.FINANCE_APPROVED,
                approvalRequest.getComment()
        ));

        //Giảm budget của Project
        Project project = request.getProject();
        BigDecimal budgetTotal = project.getBudgetTotal();
        BigDecimal budgetUsed = project.getBudgetUsed();

        project.setBudgetUsed(budgetUsed.add(request.getAmountTotal()));

        ProjectBudgetHistory budgetHistory = ProjectBudgetHistory.builder().actor(financeUser).deltaAmount(budgetTotal.subtract(budgetUsed)).project(project).reason("Duyệt tiền Project").build();


        project.setBudgetTotal(budgetTotal.subtract(request.getAmountTotal()));

               
        projectBudgetHistoryRepository.save(budgetHistory);


        return expenseMapper.toResponse(expenseRepository.save(request));
    }

    // Finance từ chối (EPIC 04)
    @Transactional
    public ExpenseRequestResponse rejectFinanceRequest(Long requestId, User financeUser, ApprovalRequest approvalRequest) {
        if (approvalRequest.getComment() == null || approvalRequest.getComment().isBlank()) {
            throw new BadRequestException("Phải cung cấp lý do khi từ chối");
        }

        ExpenseRequest request = findRequestByIdAndCheckStatus(requestId, ExpenseStatus.PENDING_FINANCE);

        request.setStatus(ExpenseStatus.FINANCE_REJECTED);
        request.addHistory(new RequestHistory(
                request,
                financeUser,
                HistoryAction.FINANCE_REJECTED,
                approvalRequest.getComment()
        ));

        return expenseMapper.toResponse(expenseRepository.save(request));
    }

    // Finance đánh dấu đã thanh toán (EPIC 04)
    @Transactional
    public ExpenseRequestResponse markAsPaid(Long requestId, User financeUser) {
        ExpenseRequest request = findRequestByIdAndCheckStatus(requestId, ExpenseStatus.APPROVED);

        request.setStatus(ExpenseStatus.PAID); // Hoàn thành
        request.addHistory(new RequestHistory(
                request,
                financeUser,
                HistoryAction.MARKED_AS_PAID,
                "Đã xử lý thanh toán"
        ));
        
    
        accountingIntegrationService.syncToAccountingSystem(request, financeUser);
        // ---------------------------

        return expenseMapper.toResponse(expenseRepository.save(request));
    }

    // === Private Helper ===

    private ExpenseRequest findRequestByIdAndCheckStatus(Long requestId, ExpenseStatus expectedStatus) {
        ExpenseRequest request = expenseRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy request với ID: " + requestId));

        if(request.getStatus() != expectedStatus) {
            throw new BadRequestException("Request không ở trạng thái " + expectedStatus);
        }
        return request;
    }

    public record DepartmentSpendingDto(String departmentName, BigDecimal totalAmount) {}

// 2. Thống kê chi tiêu theo phòng ban (Chỉ tính các đơn đã Approved/Paid)
public List<DepartmentSpendingDto> getSpendingByDepartment() {
    // Logic query đơn giản bằng Java stream (hoặc có thể viết JPQL custom)
    List<ExpenseRequest> approvedRequests = expenseRepository.findByStatusInOrderByUpdatedAtDesc(
            List.of(ExpenseStatus.APPROVED, ExpenseStatus.PAID)
    );

    Map<String, BigDecimal> stats = approvedRequests.stream()
            .collect(Collectors.groupingBy(
                    req -> req.getRequester().getDepartment() != null ? req.getRequester().getDepartment().getName() : "Unknown",
                    Collectors.reducing(BigDecimal.ZERO, ExpenseRequest::getAmountTotal, BigDecimal::add)
            ));

    return stats.entrySet().stream()
            .map(entry -> new DepartmentSpendingDto(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount())) // Sort giảm dần
            .collect(Collectors.toList());
}

// 3. Export CSV Data
public byte[] exportExpenseToCsv() {
    List<ExpenseRequest> requests = expenseRepository.findAll();
    StringBuilder csv = new StringBuilder();
    csv.append("ID,Date,Title,Requester,Department,Amount,Currency,Status\n");

    for (ExpenseRequest req : requests) {
        csv.append(String.format("%d,%s,\"%s\",\"%s\",\"%s\",%.2f,%s,%s\n",
                req.getId(),
                req.getCreatedAt().toLocalDate(),
                req.getTitle().replace("\"", "\"\""), // Escape quotes
                req.getRequester().getFullName(),
                req.getRequester().getDepartment() != null ? req.getRequester().getDepartment().getName() : "-",
                req.getAmountTotal(),
                req.getCurrency(),
                req.getStatus()
        ));
    }
    return csv.toString().getBytes(StandardCharsets.UTF_8);
}

}