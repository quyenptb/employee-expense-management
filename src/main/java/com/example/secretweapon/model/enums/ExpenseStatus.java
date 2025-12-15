package com.example.secretweapon.model.enums;


public enum ExpenseStatus {
    DRAFT, // 0. Nháp (Employee tạo)
    PENDING_MANAGER, // 1. Chờ Manager duyệt (Employee gửi)
    PENDING_FINANCE, // 2. Chờ Finance duyệt (Manager duyệt)
    APPROVED, // 3. Đã duyệt chi (Finance duyệt)
    MANAGER_REJECTED, // 4. Manager từ chối
    FINANCE_REJECTED, // 5. Finance từ chối
    PAID,// 6. Đã thanh toán (Finance xác nhận)

}