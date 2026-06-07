package com.unyt.gym.repository;

import com.unyt.gym.entity.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {

    Optional<WaterLog> findByMemberIdAndDate(Long memberId, LocalDate date);

    List<WaterLog> findByMemberIdAndDateBetweenOrderByDateDesc(Long memberId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(AVG(w.glasses), 0) FROM WaterLog w WHERE w.member.id = ?1 AND w.date >= ?2")
    double averageGlassesSince(Long memberId, LocalDate since);
}
