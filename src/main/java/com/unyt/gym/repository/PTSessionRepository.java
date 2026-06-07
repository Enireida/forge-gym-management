package com.unyt.gym.repository;

import com.unyt.gym.entity.PTSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PTSessionRepository extends JpaRepository<PTSession, Long> {
    List<PTSession> findBySubscriptionIdOrderBySessionDateDesc(Long subscriptionId);
    List<PTSession> findByTrainerIdOrderBySessionDateDesc(Long trainerId);
}
