package com.unyt.gym.repository;

import com.unyt.gym.entity.TrainerSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TrainerSlotRepository extends JpaRepository<TrainerSlot, Long> {

    List<TrainerSlot> findByTrainerIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            Long trainerId, LocalDate from, LocalDate to);

    List<TrainerSlot> findByStatusAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
            String status, LocalDate date);

    List<TrainerSlot> findByBookedByMemberIdOrderBySlotDateDesc(Long memberId);
}
