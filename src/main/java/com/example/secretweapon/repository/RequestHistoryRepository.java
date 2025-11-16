package com.example.secretweapon.repository;

import com.example.secretweapon.model.entity.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repository này chủ yếu để ghi, nhưng có thể dùng để truy vấn lịch sử sau này
@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {

}