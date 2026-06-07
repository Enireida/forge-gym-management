package com.unyt.gym.repository;

import com.unyt.gym.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByMemberIdAndDateOrderByCreatedAtAsc(Long memberId, LocalDate date);

    List<FoodLog> findByMemberIdAndDateBetweenOrderByDateAsc(Long memberId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(f.calories), 0) FROM FoodLog f WHERE f.member.id = :memberId AND f.date = :date")
    Integer sumCaloriesForDay(@Param("memberId") Long memberId, @Param("date") LocalDate date);
}
