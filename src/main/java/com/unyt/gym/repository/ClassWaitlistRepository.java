package com.unyt.gym.repository;

import com.unyt.gym.entity.ClassWaitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassWaitlistRepository extends JpaRepository<ClassWaitlist, Long> {

    List<ClassWaitlist> findByGymClassIdOrderByPositionAsc(Long classId);

    Optional<ClassWaitlist> findByGymClassIdAndMemberId(Long classId, Long memberId);

    long countByGymClassId(Long classId);

    void deleteByGymClassIdAndMemberId(Long classId, Long memberId);
}
