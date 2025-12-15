package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.ProjectHealthDTO;
import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.Project;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.repository.ExpenseRequestRepository;
import com.example.secretweapon.repository.ProjectRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticsService {

    private final ProjectRepository projectRepository;
    private final ExpenseRequestRepository expenseRequestRepository;

    @Data
    @Builder
    public static class ForecastDataPoint {
        private LocalDate date;
        private BigDecimal amount;
        private String type; // "ACTUAL", "PREDICTED", "BUDGET_CAP"
    }

    @Data
    @Builder
    public static class ProjectAnalyticsDTO {
        private Long projectId;
        private String projectName;
        private BigDecimal budgetTotal;
        private List<ForecastDataPoint> chartData;
        private LocalDate estimatedDepletionDate;
        private String trendMessage; // "Spending is accelerating!"
    }

    public ProjectAnalyticsDTO getProjectForecast(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 1. Get raw data and Group by Date
        List<ExpenseRequest> expenses = expenseRequestRepository.searchRequests(
                null, projectId, null, null, null
        ).stream().filter(e ->
                e.getStatus() == ExpenseStatus.APPROVED ||
                e.getStatus() == ExpenseStatus.PAID
        ).sorted(Comparator.comparing(ExpenseRequest::getCreatedAt)).toList();

        if (expenses.isEmpty()) {
            return ProjectAnalyticsDTO.builder()
                    .projectId(projectId)
                    .projectName(project.getName())
                    .budgetTotal(project.getBudgetTotal())
                    .chartData(Collections.emptyList())
                    .trendMessage("Not enough data to forecast")
                    .build();
        }

        // 2. Chuẩn bị dữ liệu tích lũy (Cumulative Data) cho Time Series
        // Map: Ngày thứ X (tính từ start) -> Tổng tiền đã tiêu đến ngày đó
        Map<Integer, BigDecimal> timeSeries = new TreeMap<>();
        LocalDate startDate = project.getStartDate() != null ? project.getStartDate().toLocalDate() :
                expenses.get(0).getCreatedAt().toLocalDate();

        BigDecimal cumulative = BigDecimal.ZERO;
        List<ForecastDataPoint> chartData = new ArrayList<>();

        // Add điểm bắt đầu
        chartData.add(new ForecastDataPoint(startDate, BigDecimal.ZERO, "ACTUAL"));

        // Add các điểm thực tế
        for (ExpenseRequest exp : expenses) {
            cumulative = cumulative.add(exp.getAmountTotal());
            LocalDate expDate = exp.getCreatedAt().toLocalDate();
            long daysFromStart = ChronoUnit.DAYS.between(startDate, expDate);
            
            timeSeries.put((int) daysFromStart, cumulative);
            
            // Chỉ add vào chart point cuối cùng của ngày đó (để chart đỡ rối)
            chartData.add(new ForecastDataPoint(expDate, cumulative, "ACTUAL"));
        }

        // 3. Simple Linear Regression (Least Squares Method)
        // Công thức: y = mx + c
        // m (slope) = (N * Σ(xy) - Σx * Σy) / (N * Σ(x^2) - (Σx)^2)
        // c (intercept) = (Σy - m * Σx) / N

        int n = timeSeries.size();
        if (n < 2) {
             return ProjectAnalyticsDTO.builder().projectId(projectId).chartData(chartData).trendMessage("Need more data points").build();
        }

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (Map.Entry<Integer, BigDecimal> entry : timeSeries.entrySet()) {
            double x = entry.getKey();
            double y = entry.getValue().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // 4. Dự báo (Forecast) - Extrapolate
        // Tìm ngày mà y >= budgetTotal ->  budget = slope * x + intercept => x = (budget - intercept) / slope
        LocalDate depletionDate = null;
        BigDecimal budgetTotal = project.getBudgetTotal();

        if (slope > 0) { // Nếu slope <= 0 nghĩa là không tiêu gì hoặc tiền giảm (vô lý trong ngữ cảnh này)
            double daysToDepletion = (budgetTotal.doubleValue() - intercept) / slope;
            
            // Limit forecast range (ví dụ: tối đa 6 tháng tới để tránh biểu đồ quá dài)
            double maxForecastDays = Math.max(daysToDepletion, 30); 
            
            if (daysToDepletion > 0) {
                 depletionDate = startDate.plusDays((long) daysToDepletion);
                 
                 // Add Predicted Points (Điểm cuối dự báo)
                 chartData.add(new ForecastDataPoint(
                     depletionDate, 
                     budgetTotal, 
                     "PREDICTED"
                 ));
            }
        }

        // 5. Trend Message
        String message = "Spending is stable.";
        if (slope > (budgetTotal.doubleValue() / 30)) { // Ví dụ: Tiêu hết budget trong 1 tháng
            message = "Spending is accelerating rapidly!";
        } else if (slope < (budgetTotal.doubleValue() / 365)) {
             message = "Very healthy spending pace.";
        }

        return ProjectAnalyticsDTO.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .budgetTotal(budgetTotal)
                .chartData(chartData)
                .estimatedDepletionDate(depletionDate)
                .trendMessage(message)
                .build();
    }


    public ProjectHealthDTO getProjectHealth(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        BigDecimal total = project.getBudgetTotal();
        BigDecimal used = project.getBudgetUsed();

        // 1. Tính % đã dùng
        double percentageUsed = 0.0;
        if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = used.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
        }

        // 2. Tính Burn Rate (Trung bình tiêu hao mỗi ngày từ lúc bắt đầu)
        LocalDate startDate = (project.getStartDate() != null) ? project.getStartDate().toLocalDate() : project.getCreatedAt().toLocalDate();
        long daysRun = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        if (daysRun < 1) daysRun = 1; // Tránh chia cho 0 nếu dự án mới tạo hôm nay

        BigDecimal burnRateDaily = used.divide(BigDecimal.valueOf(daysRun), 2, RoundingMode.HALF_UP);

        // 3. Tính Runway (Còn bao nhiêu ngày thì hết tiền)
        Integer daysLeft = null;
        BigDecimal remainingBudget = total.subtract(used);
        
        if (burnRateDaily.compareTo(BigDecimal.ZERO) > 0 && remainingBudget.compareTo(BigDecimal.ZERO) > 0) {
            daysLeft = remainingBudget.divide(burnRateDaily, 0, RoundingMode.HALF_UP).intValue();
        }

        // 4. Xác định Health Status
        String status = "HEALTHY";
        if (percentageUsed >= 90 || (daysLeft != null && daysLeft < 7)) {
            status = "CRITICAL"; // Hết tiền hoặc sắp hết trong 1 tuần
        } else if (percentageUsed >= 75 || (daysLeft != null && daysLeft < 30)) {
            status = "WARNING"; // Đã dùng 75% hoặc sắp hết trong 1 tháng
        }

        return ProjectHealthDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .budgetTotal(total)
                .budgetUsed(used)
                .burnRateDaily(burnRateDaily)
                .daysLeftUntilDepletion(daysLeft)
                .healthStatus(status)
                .percentageUsed(percentageUsed)
                .build();
    }


    
}