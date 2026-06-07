package com.unyt.gym.repository;

import com.unyt.gym.entity.Attendance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Attendance}.
 *
 * <p>Includes a pessimistic-write query used by {@code AttendanceService}
 * to guarantee that two concurrent kiosk taps cannot insert two open
 * attendance rows for the same member on the same day (see report
 * Section 4.1 — concurrency on the door kiosk).</p>
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByDateBetween(LocalDate from, LocalDate to);

    List<Attendance> findByMemberIdAndDateAndCheckOutIsNull(Long memberId, LocalDate date);

    /** All attendance for a member ordered by date desc */
    List<Attendance> findByMemberIdOrderByDateDesc(Long memberId);

    /** Count check-ins for member on or after a given date */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.member.id = :memberId AND a.date >= :fromDate")
    long countByMemberIdAndDateAfter(@Param("memberId") Long memberId,
                                     @Param("fromDate") LocalDate fromDate);

    /**
     * Locks the currently-open attendance row (if any) for a given member
     * and date with PESSIMISTIC_WRITE.
     *
     * <p>The kiosk check-in/check-out flow calls this query inside a
     * {@code @Transactional} method so two RFID taps that arrive on
     * different request threads do not race to insert two open rows.
     * The second thread blocks at the database row-lock until the first
     * one commits, then re-reads the up-to-date state.</p>
     *
     * <p><b>Course requirement:</b> R5 Concurrency — demonstrates
     * thread-safety awareness at the persistence layer.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Attendance a " +
           "WHERE a.member.id = :memberId " +
           "  AND a.date = :date " +
           "  AND a.checkOut IS NULL")
    Optional<Attendance> lockOpenRow(@Param("memberId") Long memberId,
                                     @Param("date") LocalDate date);
}
