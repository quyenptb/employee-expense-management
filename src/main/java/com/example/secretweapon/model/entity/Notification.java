package com.example.secretweapon.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.secretweapon.model.enums.NotificationStatus;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification {
    //id, target_user_id, channel, template_key, payload(json), state (PENDING/SENT/FAILED), created_at

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_user_id", unique = true, nullable = false, length = 255)
    private Long targetUserId;

    @Column(name = "template_key")
    private String templateKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private NotificationStatus status;

    @Column(columnDefinition = "json")
    private String payload;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
