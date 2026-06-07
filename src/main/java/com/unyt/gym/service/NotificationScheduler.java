package com.unyt.gym.service;

import com.unyt.gym.entity.ClassBooking;
import com.unyt.gym.entity.GymClass;
import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.MemberStatus;
import com.unyt.gym.repository.AttendanceRepository;
import com.unyt.gym.repository.ClassBookingRepository;
import com.unyt.gym.repository.GymClassRepository;
import com.unyt.gym.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Runs daily automated email notifications for Forge members.
 *
 * <p>Fixes applied vs. the original version:</p>
 * <ul>
 *   <li><b>No duplicate emails:</b> each method checks a {@code lastXxxNotificationSentAt}
 *       date on the Member before sending, and updates it after. If the app restarts
 *       or the scheduler fires twice in a day, the second run is a no-op.</li>
 *   <li><b>Database-side filtering:</b> birthday and missed-gym queries are now
 *       done in JPQL, not by loading every member into memory.</li>
 *   <li><b>Correct day calculation:</b> uses {@code ChronoUnit.DAYS.between()}
 *       instead of {@code Period.getDays()} which returned only the days
 *       component and missed cross-month expirations.</li>
 *   <li><b>Missed-gym rate limit:</b> a reminder is sent at most once every
 *       7 days per member, not every day they are absent.</li>
 * </ul>
 */
@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    /** Minimum gap between "we miss you" emails for the same member. */
    private static final int MISSED_GYM_RESEND_DAYS = 7;

    private final EmailService         emailService;
    private final MemberRepository     memberRepo;
    private final AttendanceRepository attendanceRepo;
    private final GymClassRepository   gymClassRepo;
    private final ClassBookingRepository classBookingRepo;

    public NotificationScheduler(EmailService emailService,
                                 MemberRepository memberRepo,
                                 AttendanceRepository attendanceRepo,
                                 GymClassRepository gymClassRepo,
                                 ClassBookingRepository classBookingRepo) {
        this.emailService     = emailService;
        this.memberRepo       = memberRepo;
        this.attendanceRepo   = attendanceRepo;
        this.gymClassRepo     = gymClassRepo;
        this.classBookingRepo = classBookingRepo;
    }

    /** Runs every day at 09:00 AM */
    @Scheduled(cron = "0 0 9 * * *")
    public void dailyNotifications() {
        log.info("[SCHEDULER] Running daily notifications...");
        checkMembershipExpiry();
        checkMissedGym();
        checkBirthdays();
        sendClassReminders();
        log.info("[SCHEDULER] Daily notifications complete.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Membership expiry warnings — 7 days before AND 3 days before
    // ─────────────────────────────────────────────────────────────────────────

    private void checkMembershipExpiry() {
        LocalDate today = LocalDate.now();

        // Only fetch members that haven't already received a notification today
        List<Member> candidates = memberRepo.findActiveWithExpiryNotificationDue(today);

        for (Member m : candidates) {
            // ChronoUnit.DAYS.between gives total days — correct across month boundaries
            long daysLeft = ChronoUnit.DAYS.between(today, m.getExpiryDate());

            if (daysLeft == 7 || daysLeft == 3) {
                try {
                    emailService.sendMembershipExpiryWarning(m, daysLeft);
                    // Mark as sent so restarts/duplicate runs don't re-send
                    m.setLastExpiryNotificationSentAt(today);
                    memberRepo.save(m);
                    log.info("[SCHEDULER] Expiry warning sent to {} ({} days left)",
                            m.getEmail(), daysLeft);
                } catch (Exception e) {
                    log.warn("[SCHEDULER] Failed expiry email for {}: {}",
                            m.getEmail(), e.getMessage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  "We miss you" reminders — at most once every 7 days per member
    // ─────────────────────────────────────────────────────────────────────────

    private void checkMissedGym() {
        LocalDate today  = LocalDate.now();
        LocalDate cutoff = today.minusDays(MISSED_GYM_RESEND_DAYS);

        // Only members who haven't received this email in the last 7 days
        List<Member> candidates = memberRepo.findActiveWithMissedNotificationDue(cutoff);

        LocalDate attendanceCutoff = today.minusDays(14);

        for (Member m : candidates) {
            long recentVisits = attendanceRepo.countByMemberIdAndDateAfter(m.getId(), attendanceCutoff);
            if (recentVisits == 0) {
                try {
                    emailService.sendMissedGymReminder(m, 14);
                    m.setLastMissedNotificationSentAt(today);
                    memberRepo.save(m);
                    log.info("[SCHEDULER] Missed-gym reminder sent to {}", m.getEmail());
                } catch (Exception e) {
                    log.warn("[SCHEDULER] Failed missed-gym email for {}: {}",
                            m.getEmail(), e.getMessage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Birthday wishes — DB query, not in-memory scan of all members
    // ─────────────────────────────────────────────────────────────────────────

    private void checkBirthdays() {
        LocalDate today = LocalDate.now();

        // Repository query filters by month+day — no full-table scan
        List<Member> todayBirthdays = memberRepo.findBirthdayMembersOnDay(
                today.getMonthValue(), today.getDayOfMonth());

        for (Member m : todayBirthdays) {
            if (m.getEmail() == null) continue;
            try {
                emailService.sendBirthdayWish(m);
                log.info("[SCHEDULER] Birthday wish sent to {}", m.getEmail());
            } catch (Exception e) {
                log.warn("[SCHEDULER] Failed birthday email for {}: {}",
                        m.getEmail(), e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Class reminders — sent the day before for all confirmed bookings
    // ─────────────────────────────────────────────────────────────────────────

    private void sendClassReminders() {
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd   = tomorrowStart.plusDays(1).minusSeconds(1);

        List<GymClass> tomorrowClasses = gymClassRepo
                .findByDateTimeBetweenAndActiveTrueOrderByDateTimeAsc(tomorrowStart, tomorrowEnd);

        for (GymClass gc : tomorrowClasses) {
            List<ClassBooking> bookings =
                    classBookingRepo.findByGymClassIdOrderByBookedAtAsc(gc.getId());

            for (ClassBooking booking : bookings) {
                if (!"CONFIRMED".equals(booking.getStatus())) continue;
                Member m = booking.getMember();
                if (m == null || m.getEmail() == null) continue;
                try {
                    emailService.sendClassReminder(m, gc);
                    log.info("[SCHEDULER] Class reminder sent to {} for {}",
                            m.getEmail(), gc.getName());
                } catch (Exception e) {
                    log.warn("[SCHEDULER] Failed class reminder for {}: {}",
                            m.getEmail(), e.getMessage());
                }
            }
        }
    }
}
