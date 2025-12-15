package com.example.secretweapon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // Class này dùng để tách cấu hình Auditing ra khỏi Main Application.
    // Giúp @WebMvcTest không bị lỗi vì cố gắng load JPA Bean.
}