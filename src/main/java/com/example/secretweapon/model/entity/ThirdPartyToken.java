package com.example.secretweapon.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "third_party_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", unique = true, nullable = false)
    private String provider; // Ví dụ: "ZOHO", "JIRA"

    @Column(name = "access_token", length = 2000)
    private String accessToken;

    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;

    @Column(name = "expires_in_seconds")
    private Long expiresInSeconds;

    @Column(name = "token_created_at")
    private LocalDateTime tokenCreatedAt; 

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "realm_id", nullable = true)
    private String realmId;
}