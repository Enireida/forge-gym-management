package com.unyt.gym.repository;

import com.unyt.gym.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByMemberIdOrderByDateDesc(Long memberId);
}
