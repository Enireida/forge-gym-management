package com.unyt.gym.repository;

import com.unyt.gym.entity.ClassBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassBookingRepository extends JpaRepository<ClassBooking, Long> {

    List<ClassBooking> findByMemberIdOrderByBookedAtDesc(Long memberId);

    List<ClassBooking> findByGymClassIdOrderByBookedAtAsc(Long classId);

    Optional<ClassBooking> findByMemberIdAndGymClassId(Long memberId, Long classId);

    int countByGymClassIdAndStatus(Long classId, String status);
}
