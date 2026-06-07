package com.unyt.gym.service;

import com.unyt.gym.entity.*;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.PTPackageRepository;
import com.unyt.gym.repository.PTSessionRepository;
import com.unyt.gym.repository.PTSubscriptionRepository;
import com.unyt.gym.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Operations on PT subscriptions and sessions.
 */
@Service
public class PTService {

    private final PTSubscriptionRepository subscriptionRepo;
    private final PTSessionRepository sessionRepo;
    private final MemberRepository memberRepo;
    private final StaffRepository staffRepo;
    private final PTPackageRepository packageRepo;

    public PTService(PTSubscriptionRepository subscriptionRepo,
                     PTSessionRepository sessionRepo,
                     MemberRepository memberRepo,
                     StaffRepository staffRepo,
                     PTPackageRepository packageRepo) {
        this.subscriptionRepo = subscriptionRepo;
        this.sessionRepo = sessionRepo;
        this.memberRepo = memberRepo;
        this.staffRepo = staffRepo;
        this.packageRepo = packageRepo;
    }

    @Transactional
    public PTSubscription createSubscription(Long memberId, Long trainerId, Long packageId) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new GymException("Member not found", 404));
        Staff trainer = staffRepo.findById(trainerId)
                .orElseThrow(() -> new GymException("Trainer not found", 404));
        PTPackage pkg = packageRepo.findById(packageId)
                .orElseThrow(() -> new GymException("Package not found", 404));
        return subscriptionRepo.save(new PTSubscription(member, trainer, pkg));
    }

    @Transactional
    public PTSubscription assignTrainer(Long subscriptionId, Long trainerId) {
        PTSubscription sub = subscriptionRepo.findById(subscriptionId)
                .orElseThrow(() -> new GymException("Subscription not found", 404));
        Staff trainer = staffRepo.findById(trainerId)
                .orElseThrow(() -> new GymException("Trainer not found", 404));
        sub.setTrainer(trainer);
        return subscriptionRepo.save(sub);
    }

    /**
     * Record a completed PT session and decrement the remaining counter.
     */
    @Transactional
    public PTSession recordSession(Long subscriptionId, Long trainerId, String type, String notes) {
        PTSubscription sub = subscriptionRepo.findById(subscriptionId)
                .orElseThrow(() -> new GymException("Subscription not found", 404));
        Staff trainer = staffRepo.findById(trainerId)
                .orElseThrow(() -> new GymException("Trainer not found", 404));

        if (sub.getSessionsRemaining() <= 0) {
            throw new GymException("No sessions remaining on this subscription", 400);
        }

        sub.setSessionsRemaining(sub.getSessionsRemaining() - 1);
        sub.setSessionsUsed(sub.getSessionsUsed() + 1);
        if (sub.getSessionsRemaining() == 0) {
            sub.setStatus("Completed");
        }
        subscriptionRepo.save(sub);

        return sessionRepo.save(new PTSession(sub, trainer, type, notes));
    }

    public List<PTSession> sessionsForSubscription(Long subscriptionId) {
        return sessionRepo.findBySubscriptionIdOrderBySessionDateDesc(subscriptionId);
    }

    public List<PTSession> sessionsByTrainer(Long trainerId) {
        return sessionRepo.findByTrainerIdOrderBySessionDateDesc(trainerId);
    }
}
