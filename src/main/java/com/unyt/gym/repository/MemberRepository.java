package com.unyt.gym.repository;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Member}.
 *
 * <p>All filtering that was previously done in-memory with streams now
 * lives here as JPQL queries — the database does the work, not Java heap.</p>
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findByPin(String pin);
    Optional<Member> findByRfidCode(String rfidCode);
    boolean existsByPin(String pin);
    boolean existsByRfidCode(String rfidCode);
    List<Member> findByStatus(MemberStatus status);

    @Query("SELECT m FROM Member m WHERE m.expiryDate <= :date AND m.status = 'ACTIVE'")
    List<Member> findExpiringBefore(LocalDate date);

    long countByStatus(MemberStatus status);

    /* ─── Used by the admin "notifications" dashboard ─────────────── */

    /** Members whose membership ends exactly on the given day. */
    List<Member> findByExpiryDate(LocalDate date);

    /** Members whose membership ends within a date range (inclusive). */
    List<Member> findByExpiryDateBetweenOrderByExpiryDateAsc(LocalDate from, LocalDate to);

    /** Members created (registered) within a date-time range. */
    List<Member> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    /**
     * Database-side search: filters by ACTIVE status and optionally by
     * name/email containing the query string (case-insensitive).
     * Results are ordered alphabetically by name.
     *
     * <p>Previously this was done with {@code findAll()} + Java streams,
     * loading every member into memory. This query pushes the filtering to
     * the database — much more efficient as the member list grows.</p>
     */
    @Query("SELECT m FROM Member m WHERE m.status = com.unyt.gym.entity.MemberStatus.ACTIVE " +
           "AND (:query IS NULL OR :query = '' " +
           "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY m.name ASC")
    List<Member> searchActiveByNameOrEmail(@Param("query") String query);

    /**
     * Returns all members whose birthday falls on the given month + day.
     * Used by the NotificationScheduler to send birthday wishes without
     * loading every member in the database.
     */
    @Query("SELECT m FROM Member m WHERE m.birthDate IS NOT NULL " +
           "AND MONTH(m.birthDate) = :month AND DAY(m.birthDate) = :day")
    List<Member> findBirthdayMembersOnDay(@Param("month") int month, @Param("day") int day);

    /**
     * Returns active members whose expiry notification has not been sent today.
     * Prevents duplicate emails if the scheduler runs more than once per day.
     */
    @Query("SELECT m FROM Member m WHERE m.status = com.unyt.gym.entity.MemberStatus.ACTIVE " +
           "AND m.expiryDate IS NOT NULL AND m.email IS NOT NULL " +
           "AND (m.lastExpiryNotificationSentAt IS NULL OR m.lastExpiryNotificationSentAt < :today)")
    List<Member> findActiveWithExpiryNotificationDue(@Param("today") LocalDate today);

    /**
     * Returns active members whose "we miss you" notification has not been
     * sent in the last 7 days — prevents daily spam to inactive members.
     */
    @Query("SELECT m FROM Member m WHERE m.status = com.unyt.gym.entity.MemberStatus.ACTIVE " +
           "AND m.email IS NOT NULL " +
           "AND (m.lastMissedNotificationSentAt IS NULL " +
           "OR m.lastMissedNotificationSentAt < :cutoff)")
    List<Member> findActiveWithMissedNotificationDue(@Param("cutoff") LocalDate cutoff);
}
