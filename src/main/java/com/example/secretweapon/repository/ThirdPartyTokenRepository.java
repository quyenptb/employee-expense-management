package com.example.secretweapon.repository;

import com.example.secretweapon.model.entity.ThirdPartyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThirdPartyTokenRepository extends JpaRepository<ThirdPartyToken, Long> {
    Optional<ThirdPartyToken> findByProvider(String provider);
}