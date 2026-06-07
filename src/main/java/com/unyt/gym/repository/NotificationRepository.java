package com.unyt.gym.repository;

import com.unyt.gym.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<Notification> findTop20ByMemberIdOrderByCreatedAtDesc(Long memberId);

    long countByMemberIdAndReadFalse(Long memberId);

    /** Guard against creating duplicate unread system notifications (e.g. expiry reminders). */
    boolean existsByMemberIdAndTypeAndReadFalse(Long memberId, Notification.NotifType type);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.member.id = ?1 AND n.read = false")
    void markAllReadForMember(Long memberId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.member.id = ?1")
    void deleteAllForMember(Long memberId);
}
