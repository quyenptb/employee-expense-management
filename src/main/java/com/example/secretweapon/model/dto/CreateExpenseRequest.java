package com.example.secretweapon.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// DTO cho Employee tạo request (EPIC 02)
@Data
public class CreateExpenseRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    // Client sẽ upload ảnh lên 1 dịch vụ (vd: S3, Cloudinary) và gửi URL về
    private String receiptImageUrl;
}
