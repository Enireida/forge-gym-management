package com.unyt.gym.repository;

import com.unyt.gym.entity.ClassRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassRatingRepository extends JpaRepository<ClassRating, Long> {

    List<ClassRating> findByGymClassIdOrderByCreatedAtDesc(Long classId);

    Optional<ClassRating> findByGymClassIdAndMemberId(Long classId, Long memberId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ClassRating r WHERE r.gymClass.id = ?1")
    double averageRatingForClass(Long classId);

    long countByGymClassId(Long classId);

    /** Aggregate trainer rating across all their classes. */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ClassRating r WHERE r.gymClass.trainer.id = ?1")
    double averageRatingForTrainer(Long trainerId);

    @Query("SELECT COUNT(r) FROM ClassRating r WHERE r.gymClass.trainer.id = ?1")
    long countRatingsForTrainer(Long trainerId);
}
