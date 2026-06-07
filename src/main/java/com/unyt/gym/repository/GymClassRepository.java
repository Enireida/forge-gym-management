package com.unyt.gym.repository;

import com.unyt.gym.entity.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    List<GymClass> findByActiveTrueOrderByDateTimeAsc();

    List<GymClass> findByDateTimeBetweenAndActiveTrueOrderByDateTimeAsc(LocalDateTime from, LocalDateTime to);
}
