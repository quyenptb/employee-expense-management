package com.example.secretweapon.service;

import java.math.BigDecimal;


public interface AnomalyDetectionService {

    boolean isAverageAmountValid(BigDecimal amount, String roleName);

    boolean isRequestCountValid(int count, String roleName);

    /**
     * Kiểm tra xem URL hóa đơn có bị trùng lặp với bất kỳ hóa đơn nào đã lưu hay không.
     * 
     * @param imageUrl URL của ảnh hóa đơn mới.
     * @return true nếu phát hiện trùng lặp (anomaly), false nếu là duy nhất.
     */
    boolean isReceiptDuplicate(String imageUrl);

    boolean isUserAnomaly(BigDecimal employeeAmount, int employeeRequestCount, String roleName);
}
