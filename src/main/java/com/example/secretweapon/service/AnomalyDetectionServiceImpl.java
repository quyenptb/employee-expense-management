package com.example.secretweapon.service;

import com.example.secretweapon.repository.ExpenseRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private final ExpenseRequestRepository expenseRequestRepository;
    private final ReceiptHasher receiptHasher;


    private static final Map<String, BigDecimal> ROLE_AMOUNT_THRESHOLD = Map.of(
            "EMPLOYEE", BigDecimal.valueOf(50_000_000),
            "MANAGER", BigDecimal.valueOf(100_000_000)
    );

    private static final Map<String, Integer> ROLE_REQUEST_THRESHOLD = Map.of(
            "EMPLOYEE", 10,
            "MANAGER", 20
    );

    public boolean isAverageAmountValid(BigDecimal amount, String roleName) {
        return amount.compareTo(ROLE_AMOUNT_THRESHOLD.getOrDefault(roleName, BigDecimal.valueOf(Long.MAX_VALUE))) < 0;
    }

    public boolean isRequestCountValid(int count, String roleName) {
        return count < ROLE_REQUEST_THRESHOLD.getOrDefault(roleName, Integer.MAX_VALUE);
    }

    /**
     * Kiểm tra xem URL hóa đơn có bị trùng lặp với bất kỳ hóa đơn nào đã lưu hay không.
     * 
     * @param imageUrl URL của ảnh hóa đơn mới.
     * @return true nếu phát hiện trùng lặp (anomaly), false nếu là duy nhất.
     */
    public boolean isReceiptDuplicate(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        try {
            // Thêm log để biết đang bắt đầu check
             log.info("Checking duplicate for URL: {}", imageUrl);
            String result = receiptHasher.processReceiptAsync(imageUrl).get();
            
            // Log kết quả trả về từ Hasher
             log.info("Hasher result: {}", result);

            if (result.startsWith("ALERT:")) {
                return true;
            } else {
                return false;
            }
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error checking receipt duplicate: ", e); 
            return false;
        }
    }



    public boolean isUserAnomaly(BigDecimal employeeAmount, int employeeRequestCount, String roleName) {
        return !isRequestCountValid(employeeRequestCount, roleName) || !isAverageAmountValid(employeeAmount, roleName);
    }
}
