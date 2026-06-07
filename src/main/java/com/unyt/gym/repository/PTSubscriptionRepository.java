package com.unyt.gym.repository;

import com.unyt.gym.entity.PTSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PTSubscriptionRepository extends JpaRepository<PTSubscription, Long> {
    List<PTSubscription> findByMemberId(Long memberId);
    List<PTSubscription> findByTrainerId(Long trainerId);
}
