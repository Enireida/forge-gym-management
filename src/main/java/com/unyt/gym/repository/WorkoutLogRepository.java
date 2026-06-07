package com.unyt.gym.repository;

import com.unyt.gym.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

    List<WorkoutLog> findByMemberIdOrderByStartTimeDesc(Long memberId);

    List<WorkoutLog> findByMemberIdAndStartTimeAfterOrderByStartTimeDesc(Long memberId, LocalDateTime after);

    Optional<WorkoutLog> findByHealthKitUUID(String healthKitUUID);

    boolean existsByHealthKitUUID(String healthKitUUID);

    /** Total calories burned by member in date range */
    @Query("SELECT COALESCE(SUM(w.activeCalories), 0) FROM WorkoutLog w " +
           "WHERE w.member.id = :memberId AND w.startTime >= :from AND w.startTime <= :to")
    Double sumCaloriesByMemberAndDateRange(@Param("memberId") Long memberId,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    /** Total workout seconds by member in date range */
    @Query("SELECT COALESCE(SUM(w.durationSeconds), 0) FROM WorkoutLog w " +
           "WHERE w.member.id = :memberId AND w.startTime >= :from AND w.startTime <= :to")
    Long sumDurationByMemberAndDateRange(@Param("memberId") Long memberId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    /** Count workouts by member in date range */
    long countByMemberIdAndStartTimeAfter(Long memberId, LocalDateTime after);

    /** Average heart rate for member in date range */
    @Query("SELECT COALESCE(AVG(w.avgHeartRate), 0) FROM WorkoutLog w " +
           "WHERE w.member.id = :memberId AND w.startTime >= :from AND w.avgHeartRate IS NOT NULL")
    Double avgHeartRateSince(@Param("memberId") Long memberId, @Param("from") LocalDateTime from);

    /** All workouts globally, most recent first (for social feed) */
    List<WorkoutLog> findTop30ByOrderByStartTimeDesc();

    /** Count workouts per day for a member in a range, for streak calculation */
    @Query("SELECT CAST(w.startTime AS localdate) FROM WorkoutLog w " +
           "WHERE w.member.id = :memberId AND w.startTime >= :from " +
           "GROUP BY CAST(w.startTime AS localdate) ORDER BY CAST(w.startTime AS localdate) DESC")
    List<LocalDate> findDistinctWorkoutDays(@Param("memberId") Long memberId, @Param("from") LocalDateTime from);
}
