package com.saborclick.auth.repository;

import com.saborclick.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    int deleteByUsedTrueOrExpirationBefore(LocalDateTime fecha);
}
