package com.unyt.gym.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Factory Method Pattern</b> (GoF) — produces a {@link Notification}
 * appropriate for the channel asked for (email, SMS, in-app).
 *
 * <p>This decouples the calling code (e.g. the membership-expiry
 * scheduler) from the concrete notifier implementation.</p>
 *
 * <p><b>Course requirement:</b> R9 Design Patterns.</p>
 */
public final class NotificationFactory {

    private NotificationFactory() { }

    public enum Channel { EMAIL, SMS, IN_APP }

    /**
     * @return a {@link Notification} that delivers via the requested
     *         channel
     */
    public static Notification forChannel(Channel channel) {
        return switch (channel) {
            case EMAIL  -> new EmailNotification();
            case SMS    -> new SmsNotification();
            case IN_APP -> new InAppNotification();
        };
    }

    /** Behaviour shared by every channel. */
    public interface Notification {
        void send(String to, String subject, String body);
    }

    static final class EmailNotification implements Notification {
        private static final Logger log = LoggerFactory.getLogger(EmailNotification.class);
        @Override public void send(String to, String subject, String body) {
            log.info("[EMAIL] -> {} | {} | {}", to, subject, body);
        }
    }

    static final class SmsNotification implements Notification {
        private static final Logger log = LoggerFactory.getLogger(SmsNotification.class);
        @Override public void send(String to, String subject, String body) {
            log.info("[SMS] -> {} | {}", to, body);
        }
    }

    static final class InAppNotification implements Notification {
        private static final Logger log = LoggerFactory.getLogger(InAppNotification.class);
        @Override public void send(String to, String subject, String body) {
            log.info("[IN-APP] -> {} | {} | {}", to, subject, body);
        }
    }
}
