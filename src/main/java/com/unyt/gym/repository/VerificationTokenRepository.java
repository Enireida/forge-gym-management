package com.unyt.gym.repository;

import com.unyt.gym.entity.VerificationToken;
import com.unyt.gym.entity.VerificationToken.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /** Find most recent valid (unused, not expired) token for email+type */
    Optional<VerificationToken> findTopByEmailAndTokenTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, TokenType tokenType);

    /** Delete all tokens for an email+type (cleanup on re-send) */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken t WHERE t.email = :email AND t.tokenType = :tokenType")
    void deleteAllByEmailAndTokenType(String email, TokenType tokenType);

    /** Cleanup expired tokens (can be called by a scheduler) */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :now")
    void deleteAllExpiredBefore(LocalDateTime now);
}
