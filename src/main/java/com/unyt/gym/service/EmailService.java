package com.unyt.gym.service;

import com.unyt.gym.entity.GymClass;
import com.unyt.gym.entity.Member;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Branded email service for Forge notifications.
 * In dev-mode, emails are only logged to console (no actual sending).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${forge.mail.dev-mode:false}")
    private boolean devMode;

    @Value("${forge.mail.from:}")
    private String fromAddr;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${forge.mail.admin-email:recienireida@gmail.com}")
    private String adminEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Returns the address to put in the From header. Gmail requires this to
     * match the authenticated SMTP user, so if forge.mail.from is not set we
     * fall back to spring.mail.username. This makes sending work even when the
     * from-address property is missing.
     */
    private String effectiveFrom() {
        if (fromAddr != null && !fromAddr.isBlank()) return fromAddr;
        if (mailUsername != null && !mailUsername.isBlank()) return mailUsername;
        return "noreply@forge.al";
    }

    /** Tests the SMTP connection on startup so config errors are visible immediately. */
    @EventListener(ApplicationReadyEvent.class)
    public void testSmtpOnStartup() {
        if (devMode) {
            log.info("[SMTP] dev-mode=true — emails are logged only, not sent.");
            return;
        }
        if (mailSender instanceof JavaMailSenderImpl impl) {
            try {
                impl.testConnection();
                log.info("[SMTP] Connection OK — emails will be sent from {}", effectiveFrom());
            } catch (Exception e) {
                log.error("[SMTP] Connection FAILED: {}", e.getMessage());
                Throwable c = e.getCause();
                while (c != null) { log.error("[SMTP] caused by: {}", c.getMessage()); c = c.getCause(); }
            }
        }
    }

    /**
     * Sends an HTML email. Returns {@code true} if the email was sent (or dev-mode
     * is on), {@code false} if it failed. Callers that depend on delivery (OTP,
     * registration, password reset) should check this value.
     */
    public boolean send(String to, String subject, String htmlBody) {
        if (devMode) {
            log.info("[EMAIL] dev-mode — To: {} | Subject: {}", to, subject);
            return true;
        }
        if (to == null || to.isBlank()) {
            log.warn("[EMAIL] No recipient address — skipped: {}", subject);
            return false;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(effectiveFrom(), "Forge Gym");
            h.setTo(to.trim());
            h.setSubject(subject);
            h.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("[EMAIL] Sent to {} — {}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    public void sendMembershipExpiryWarning(Member m, long daysLeft) {
        send(m.getEmail(),
             "Abonimi juaj skadon pas " + daysLeft + " diteve — Forge",
             buildExpiryHtml(m, daysLeft));
    }

    public void sendMissedGymReminder(Member m, int daysSinceLastVisit) {
        send(m.getEmail(),
             "Na mungoni, " + m.getName() + "! — Forge",
             buildMissedHtml(m, daysSinceLastVisit));
    }

    public void sendBirthdayWish(Member m) {
        send(m.getEmail(),
             "Gezuar Ditelindjen, " + m.getName() + "! — Forge",
             buildBirthdayHtml(m));
    }

    public void sendClassReminder(Member m, GymClass c) {
        send(m.getEmail(),
             "Klase sot: " + c.getName() + " — Forge",
             buildClassReminderHtml(m, c));
    }

    public void sendRegistrationPending(Member m) {
        if (m.getEmail() == null) return;
        send(m.getEmail(),
             "Forge — Regjistrimi juaj u pranua, pret konfirmim",
             buildRegistrationPendingHtml(m));
    }

    public void sendMembershipCreated(Member m) {
        if (m.getEmail() == null) return;
        send(m.getEmail(),
             "Mirë se vini në Forge — Abonimi juaj u aktivizua!",
             buildMembershipCreatedHtml(m));
    }

    public void sendMembershipUpdated(Member m) {
        if (m.getEmail() == null) return;
        send(m.getEmail(),
             "Abonimi juaj u përditësua — Forge",
             buildMembershipUpdatedHtml(m));
    }

    public void sendMembershipDeleted(Member m) {
        if (m.getEmail() == null) return;
        send(m.getEmail(),
             "Abonimi juaj në Forge u anulua",
             buildMembershipDeletedHtml(m));
    }

    public boolean sendPasswordResetOtp(String email, String name, String otp) {
        return sendPasswordResetOtp(email, name, otp, "SQ");
    }

    public boolean sendPasswordResetOtp(String email, String name, String otp, String lang) {
        boolean en = "EN".equalsIgnoreCase(lang);
        String subject = en
            ? "Your Forge password reset code"
            : "Kodi juaj i rivendosjes se fjalekalimit — Forge";
        return send(email, subject, buildOtpHtml(name, otp, en));
    }

    // ─── HTML BUILDERS ───────────────────────────────────────────────────────

    private String wrapEmail(String bodyContent) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
               "<body style='margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif'>" +
               "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f4f4;padding:32px 16px'>" +
               "<tr><td align='center'>" +
               "<table width='540' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:8px;border:1px solid #e0e0e0'>" +
               "<tr><td style='padding:24px 40px;border-bottom:1px solid #e0e0e0'>" +
               "<span style='font-size:22px;font-weight:700;color:#1a1a1a;letter-spacing:1px'>Forge</span>" +
               "<span style='display:inline-block;width:6px;height:6px;background:#c0a070;border-radius:50%;margin-left:2px;vertical-align:middle'></span>" +
               "</td></tr>" +
               "<tr><td style='padding:32px 40px'>" +
               bodyContent +
               "</td></tr>" +
               "<tr><td style='padding:16px 40px;border-top:1px solid #e0e0e0;text-align:center'>" +
               "<p style='margin:0;font-size:12px;color:#999999'>Forge Gym &bull; Tiranë, Albania &bull; reidareci1@gmail.com</p>" +
               "</td></tr>" +
               "</table>" +
               "</td></tr></table>" +
               "</body></html>";
    }

    private String ctaBtn(String text, String url) {
        return "<table cellpadding='0' cellspacing='0' style='margin:24px 0'>" +
               "<tr><td style='background:#1a1a1a;border-radius:6px;text-align:center'>" +
               "<a href='" + url + "' style='display:inline-block;padding:12px 28px;font-family:Arial,sans-serif;font-size:14px;font-weight:600;color:#ffffff;text-decoration:none'>" + text + "</a>" +
               "</td></tr></table>";
    }

    private String buildExpiryHtml(Member m, long daysLeft) {
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Abonimi juaj skadon se shpejti</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Pershendetje <strong>" + m.getName() + "</strong>,</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 20px'>Abonimi juaj ne Forge do te skadoje ne <strong>" + daysLeft + " dite</strong>. Rinovoni tani per te vazhduar ushtrimet.</p>" +
            ctaBtn("Rinovoni Abonimin", "http://localhost:8080/client/membership") +
            "<p style='font-size:13px;color:#888888;line-height:1.6'>Nese keni pyetje, kontaktoni recepsionin.</p>";
        return wrapEmail(content);
    }

    private String buildMissedHtml(Member m, int days) {
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Na mungoni, " + m.getName() + "!</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Ka kaluar <strong>" + days + " dite</strong> qe nuk kemi parë njeri-tjetrin.</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 20px'>Eja sot — instrumentet jane gati dhe ne gjithashtu.</p>" +
            ctaBtn("Shiko Dashboard-in Tim", "http://localhost:8080/client") +
            "<p style='font-size:13px;color:#888888'>Vazhdoni te qendroni aktive!</p>";
        return wrapEmail(content);
    }

    private String buildBirthdayHtml(Member m) {
        String content =
            "<p style='font-size:36px;text-align:center;margin:0 0 16px'>🎂</p>" +
            "<h2 style='font-size:20px;color:#1a1a1a;text-align:center;margin:0 0 16px'>Gezuar Ditelindjen, " + m.getName() + "!</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;text-align:center;margin:0 0 20px'>E gjithe ekipi i Forge ju uroon nje Ditelindje te lumtur dhe plot energji!</p>" +
            ctaBtn("Shiko Forge-un", "http://localhost:8080/client") +
            "<p style='font-size:13px;color:#888888;text-align:center'>Forge Gym — Bashke per cdo qellim fitness!</p>";
        return wrapEmail(content);
    }

    private String buildOtpHtml(String name, String otp) {
        return buildOtpHtml(name, otp, false);
    }

    private String buildOtpHtml(String name, String otp, boolean en) {
        StringBuilder digits = new StringBuilder();
        for (char ch : otp.toCharArray()) {
            digits.append("<span style='display:inline-block;width:44px;height:56px;line-height:56px;" +
                "border:2px solid #cccccc;border-radius:6px;text-align:center;" +
                "font-family:monospace;font-size:28px;font-weight:700;color:#1a1a1a;" +
                "background:#f9f9f9;margin:0 4px'>").append(ch).append("</span>");
        }
        String title   = en ? "Reset your password"          : "Rivendosni fjalekalimin";
        String hello   = en ? "Hello"                         : "Pershendetje";
        String body    = en ? "Your password reset code:"     : "Kodi juaj i rivendosjes se fjalekalimit:";
        String expires = en ? "This code expires in <strong>15 minutes</strong>."
                            : "Ky kod skadon pas <strong>15 minutash</strong>.";
        String ignore  = en ? "If you didn't request this, you can safely ignore this email."
                            : "Nese nuk e keni kerkuar ju, injoroni kete email.";
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>" + title + "</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 8px'>" + hello + " <strong>" + name + "</strong>,</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 24px'>" + body + "</p>" +
            "<div style='text-align:center;margin:0 0 24px'>" + digits + "</div>" +
            "<p style='font-size:13px;color:#888888;text-align:center;margin:0 0 8px'>" + expires + "</p>" +
            "<p style='font-size:13px;color:#888888;text-align:center'>" + ignore + "</p>";
        return wrapEmail(content);
    }

    private String buildRegistrationPendingHtml(Member m) {
        String planName = m.getPlan() != null ? m.getPlan().getName() : "—";
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Regjistrimi juaj u pranua!</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Pershendetje <strong>" + m.getName() + "</strong>,</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 16px'>Llogaria juaj ne Forge u krijua me sukses. Plani i zgjedhur: <strong>" + planName + "</strong>.</p>" +
            "<div style='background:#fff8e1;border:1px solid #ffe082;border-radius:6px;padding:14px 16px;margin:0 0 20px'>" +
            "<p style='margin:0;font-size:14px;color:#5d4037;line-height:1.6'><strong>Ne pritje te konfirmimit.</strong> Ekipi i Forge do te konfirmoje llogarinë tuaj pasi te verifikoje pagesen. Do te merrni nje email konfirmimi.</p>" +
            "</div>" +
            "<p style='font-size:13px;color:#888888'>Nese keni pyetje, vizitoni recepsionin e palestrës.</p>";
        return wrapEmail(content);
    }

    private String buildMembershipCreatedHtml(Member m) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);
        String planName   = m.getPlan() != null ? m.getPlan().getName() : "—";
        String startDate  = m.getStartDate()  != null ? m.getStartDate().format(fmt)  : "—";
        String expiryDate = m.getExpiryDate() != null ? m.getExpiryDate().format(fmt) : "—";
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Mire se erdhet ne Forge!</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Pershendetje <strong>" + m.getName() + "</strong>,</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 20px'>Abonimi juaj u krijua me sukses. Ja detajet:</p>" +
            "<table width='100%' cellpadding='12' cellspacing='0' style='border:1px solid #e0e0e0;border-radius:6px;margin:0 0 24px'>" +
            "<tr><td style='border-bottom:1px solid #e0e0e0'><span style='font-size:12px;color:#888888'>Plani</span><br><strong style='font-size:15px;color:#1a1a1a'>" + planName + "</strong></td></tr>" +
            "<tr><td style='border-bottom:1px solid #e0e0e0'><span style='font-size:12px;color:#888888'>Data e fillimit</span><br><strong style='font-size:15px;color:#1a1a1a'>" + startDate + "</strong></td></tr>" +
            "<tr><td><span style='font-size:12px;color:#888888'>Data e skadimit</span><br><strong style='font-size:15px;color:#1a1a1a'>" + expiryDate + "</strong></td></tr>" +
            "</table>" +
            ctaBtn("Shiko Profilin Tim", "http://localhost:8080/client") +
            "<p style='font-size:13px;color:#888888'>Nese keni pyetje, kontaktoni recepsionin.</p>";
        return wrapEmail(content);
    }

    private String buildMembershipUpdatedHtml(Member m) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT);
        String planName   = m.getPlan() != null ? m.getPlan().getName() : "—";
        String expiryDate = m.getExpiryDate() != null ? m.getExpiryDate().format(fmt) : "—";
        String status     = m.getStatus() != null ? m.getStatus().name() : "—";
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Abonimi juaj u perditesua</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Pershendetje <strong>" + m.getName() + "</strong>,</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 20px'>Detajet e abonimit tuaj u ndryshuan. Gjendja aktuale:</p>" +
            "<table width='100%' cellpadding='12' cellspacing='0' style='border:1px solid #e0e0e0;border-radius:6px;margin:0 0 24px'>" +
            "<tr><td style='border-bottom:1px solid #e0e0e0'><span style='font-size:12px;color:#888888'>Plani</span><br><strong style='font-size:15px;color:#1a1a1a'>" + planName + "</strong></td></tr>" +
            "<tr><td style='border-bottom:1px solid #e0e0e0'><span style='font-size:12px;color:#888888'>Skadon me</span><br><strong style='font-size:15px;color:#1a1a1a'>" + expiryDate + "</strong></td></tr>" +
            "<tr><td><span style='font-size:12px;color:#888888'>Statusi</span><br><strong style='font-size:15px;color:#1a1a1a'>" + status + "</strong></td></tr>" +
            "</table>" +
            "<p style='font-size:13px;color:#888888'>Nese ky ndryshim nuk u be nga ju, kontaktoni recepsionin menjehere.</p>";
        return wrapEmail(content);
    }

    private String buildMembershipDeletedHtml(Member m) {
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Abonimi juaj u anulua</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Pershendetje <strong>" + m.getName() + "</strong>,</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 12px'>Abonimi juaj ne Forge eshte anuluar. Do te na mungoni!</p>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 20px'>Nese deshironi te riaktivizoni anetaresine, vizitoni recepsionin.</p>" +
            "<p style='font-size:13px;color:#888888'>Ju falemenderojme per kohen qe kaluat me ne!</p>";
        return wrapEmail(content);
    }

    private String buildClassReminderHtml(Member m, GymClass c) {
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'ora' HH:mm");
        String content =
            "<h2 style='font-size:20px;color:#1a1a1a;margin:0 0 16px'>Kujtuese: Klase neser!</h2>" +
            "<p style='font-size:15px;color:#333333;line-height:1.6;margin:0 0 20px'>Pershendetje <strong>" + m.getName() + "</strong>, keni nje klase te rezervuar per neser:</p>" +
            "<table width='100%' cellpadding='12' cellspacing='0' style='border:1px solid #e0e0e0;border-radius:6px;margin:0 0 24px'>" +
            "<tr><td style='border-bottom:1px solid #e0e0e0'><span style='font-size:12px;color:#888888'>Klasa</span><br><strong style='font-size:15px;color:#1a1a1a'>" + c.getName() + "</strong></td></tr>" +
            "<tr><td style='border-bottom:1px solid #e0e0e0'><span style='font-size:12px;color:#888888'>Data &amp; Ora</span><br><strong style='font-size:15px;color:#1a1a1a'>" + (c.getDateTime() != null ? c.getDateTime().format(dtFmt) : "TBD") + "</strong></td></tr>" +
            "<tr><td><span style='font-size:12px;color:#888888'>Kategoria</span><br><strong style='font-size:15px;color:#1a1a1a'>" + (c.getCategory() != null ? c.getCategory() : "General") + "</strong></td></tr>" +
            "</table>" +
            ctaBtn("Shiko Orarin", "http://localhost:8080/client/classes") +
            "<p style='font-size:13px;color:#888888'>Jini gati — shihen neser ne Forge!</p>";
        return wrapEmail(content);
    }
}
