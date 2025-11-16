package com.example.secretweapon.model.enums;


// Enum định nghĩa State Machine (Máy trạng thái) cho một request
public enum ExpenseStatus {
    DRAFT, // 0. Nháp (Employee tạo)
    PENDING_MANAGER, // 1. Chờ Manager duyệt (Employee gửi)
    PENDING_FINANCE, // 2. Chờ Finance duyệt (Manager duyệt)
    APPROVED, // 3. Đã duyệt chi (Finance duyệt)
    REJECTED_MANAGER, // 4. Manager từ chối
    REJECTED_FINANCE, // 5. Finance từ chối
    PAID // 6. Đã thanh toán (Finance xác nhận)
}