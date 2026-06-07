package com.unyt.gym.config;

import com.unyt.gym.entity.Attendance;
import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.Payment;
import com.unyt.gym.jdbc.RawReportDao;
import com.unyt.gym.pattern.EventBus;
import com.unyt.gym.pattern.NotificationFactory;
import com.unyt.gym.pattern.NotificationFactory.Channel;
import com.unyt.gym.pattern.NotificationFactory.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Wires up real subscribers to the {@link EventBus} on application start.
 *
 * <p>Without this class, the Observer pattern would be only theoretical:
 * services would publish events into a registry with no listeners. This
 * component closes the loop by registering three subscribers, each
 * mapping a domain event to a concrete side-effect:</p>
 *
 * <ol>
 *   <li>{@code member.created} -> JDBC audit row + welcome notification.</li>
 *   <li>{@code payment.received} -> JDBC audit row + email confirmation
 *       to the member (routed through {@link NotificationFactory}).</li>
 *   <li>{@code attendance.checkin} / {@code attendance.checkout} ->
 *       JDBC audit row only (high volume — no notification to avoid
 *       spam).</li>
 * </ol>
 *
 * <p>Listeners are intentionally small: the heavy lifting (DB I/O,
 * notification delivery) lives in the services they call. This keeps the
 * Observer wiring readable and makes it easy to add a new listener for a
 * new event without touching any publisher.</p>
 *
 * <p><b>Course requirement:</b> R9 Observer pattern — completes the
 * publish/subscribe loop with concrete subscribers. Also brings together
 * R6 (JDBC audit log) and R9 (Factory-built notifications) in one place.</p>
 */
@Component
public class EventListenerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(EventListenerRegistrar.class);

    private final RawReportDao auditDao;

    public EventListenerRegistrar(RawReportDao auditDao) {
        this.auditDao = auditDao;
    }

    @PostConstruct
    void registerSubscribers() {
        // Make sure the audit_log table exists before the first event arrives.
        auditDao.ensureAuditTable();

        EventBus bus = EventBus.getInstance();

        // --- member.created ---------------------------------------------------
        bus.<Member>subscribe("member.created", member -> {
            auditDao.insertAudit(
                    "system",
                    "MEMBER_CREATED",
                    "Member '" + member.getName() + "' (id=" + member.getId() + ") was created.");

            // Use the Factory pattern (R9) to pick a notification channel.
            Notification welcome = NotificationFactory.forChannel(Channel.EMAIL);
            welcome.send(
                    member.getEmail(),
                    "Welcome to Forge",
                    "Hi " + member.getName() + ", your account is active. "
                            + "Your check-in PIN is " + member.getPin() + ".");

            log.info("[event] member.created -> id={}, email={}",
                    member.getId(), member.getEmail());
        });

        // --- payment.received -------------------------------------------------
        bus.<Payment>subscribe("payment.received", payment -> {
            String memberName = payment.getMember() != null
                    ? payment.getMember().getName() : "(unknown)";
            auditDao.insertAudit(
                    memberName,
                    "PAYMENT_RECEIVED",
                    "Amount " + payment.getAmount() + " via " + payment.getMethod());

            Notification email = NotificationFactory.forChannel(Channel.EMAIL);
            if (payment.getMember() != null) {
                email.send(
                        payment.getMember().getEmail(),
                        "Payment received",
                        "Hi " + memberName + ", we received your payment of "
                                + payment.getAmount() + ". Thank you!");
            }
            log.info("[event] payment.received -> member={}, amount={}",
                    memberName, payment.getAmount());
        });

        // --- attendance.checkin / checkout -----------------------------------
        bus.<Attendance>subscribe("attendance.checkin", att -> {
            String memberName = att.getMember() != null
                    ? att.getMember().getName() : "(unknown)";
            auditDao.insertAudit(
                    memberName, "CHECK_IN",
                    "Checked in at " + att.getCheckIn());
            log.debug("[event] attendance.checkin -> {}", memberName);
        });

        bus.<Attendance>subscribe("attendance.checkout", att -> {
            String memberName = att.getMember() != null
                    ? att.getMember().getName() : "(unknown)";
            auditDao.insertAudit(
                    memberName, "CHECK_OUT",
                    "Checked out at " + att.getCheckOut());
            log.debug("[event] attendance.checkout -> {}", memberName);
        });

        log.info("EventBus subscribers registered (member.created, payment.received, attendance.*).");
    }
}
