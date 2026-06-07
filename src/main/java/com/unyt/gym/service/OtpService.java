package com.unyt.gym.service;

import com.unyt.gym.entity.VerificationToken;
import com.unyt.gym.entity.VerificationToken.TokenType;
import com.unyt.gym.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * OTP (One-Time Password) service for email verification and 2FA.
 *
 * Flow:
 *  1. Call sendOtp(email, type)  → generates 6-digit code, saves to DB, sends email
 *  2. Call verifyOtp(email, code, type) → returns true if valid and marks as used
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final VerificationTokenRepository tokenRepo;
    private final JavaMailSender mailSender;

    public OtpService(VerificationTokenRepository tokenRepo, JavaMailSender mailSender) {
        this.tokenRepo   = tokenRepo;
        this.mailSender  = mailSender;
    }

    @Value("${forge.mail.dev-mode:false}")
    private boolean devMode;

    @Value("${forge.mail.from:}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /** Gmail requires From to match the authenticated user; fall back to it if from is unset. */
    private String effectiveFrom() {
        if (fromAddress != null && !fromAddress.isBlank()) return fromAddress;
        if (mailUsername != null && !mailUsername.isBlank()) return mailUsername;
        return "noreply@forge.al";
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    // ─────────────────────────────────────────────
    //  SEND OTP
    // ─────────────────────────────────────────────

    /**
     * Generate a 6-digit OTP, save it, and send the email.
     * Deletes any previous pending tokens of the same type for this email.
     *
     * @return the generated code (useful for dev/testing)
     */
    public String sendOtp(String email, TokenType type) {
        // Delete previous tokens of same type
        tokenRepo.deleteAllByEmailAndTokenType(email, type);

        // Generate 6-digit code
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        // Save token
        VerificationToken token = new VerificationToken(code, email, type);
        tokenRepo.save(token);

        // Send email
        if (devMode) {
            // In dev mode: just log the code
            log.info("═══════════════════════════════════════");
            log.info("  FORGE OTP [DEV MODE — not sent]");
            log.info("  Email : {}", email);
            log.info("  Type  : {}", type);
            log.info("  Code  : {}", code);
            log.info("  Expires in 10 minutes");
            log.info("═══════════════════════════════════════");
        } else {
            sendEmail(email, type, code);
        }

        return code;
    }

    // ─────────────────────────────────────────────
    //  VERIFY OTP
    // ─────────────────────────────────────────────

    /**
     * Verify an OTP code for an email+type combination.
     * Marks the token as used if valid.
     *
     * @return true if valid, false otherwise
     */
    public boolean verifyOtp(String email, String code, TokenType type) {
        Optional<VerificationToken> opt =
            tokenRepo.findTopByEmailAndTokenTypeAndUsedFalseOrderByCreatedAtDesc(email, type);

        if (opt.isEmpty()) {
            log.warn("OTP verify: no active token found for {} / {}", email, type);
            return false;
        }

        VerificationToken token = opt.get();

        if (token.isExpired()) {
            log.warn("OTP verify: token expired for {}", email);
            return false;
        }

        if (!token.getCode().equals(code.trim())) {
            log.warn("OTP verify: wrong code for {}", email);
            return false;
        }

        // Mark as used
        token.setUsed(true);
        tokenRepo.save(token);
        log.info("OTP verified successfully for {} / {}", email, type);
        return true;
    }

    // ─────────────────────────────────────────────
    //  EMAIL SENDING
    // ─────────────────────────────────────────────

    private void sendEmail(String toEmail, TokenType type, String code) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(effectiveFrom(), "Forge");
            helper.setTo(toEmail);
            helper.setSubject(subjectFor(type));
            helper.setText(buildHtml(code, type), true);

            mailSender.send(msg);
            log.info("OTP email sent to {} for type {}", toEmail, type);

        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Could not send verification email. Please try again.", e);
        }
    }

    private String subjectFor(TokenType type) {
        return switch (type) {
            case EMAIL_VERIFY  -> "Forge — Verify your email address";
            case LOGIN_2FA     -> "Forge — Your login code";
            case PASSWORD_RESET -> "Forge — Reset your password";
        };
    }

    private String buildHtml(String code, TokenType type) {
        String heading = switch (type) {
            case EMAIL_VERIFY   -> "Verify your email";
            case LOGIN_2FA      -> "Your login code";
            case PASSWORD_RESET -> "Reset your password";
        };
        String subtext = switch (type) {
            case EMAIL_VERIFY   -> "Enter this code to verify your email address.";
            case LOGIN_2FA      -> "Enter this code to complete your sign-in.";
            case PASSWORD_RESET -> "Enter this code to reset your password.";
        };

        // Inline HTML email — matches Forge design (navy + copper)
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width,initial-scale=1"/>
          <title>%s — Forge</title>
        </head>
        <body style="margin:0;padding:0;background:#F7F8FA;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background:#F7F8FA;padding:48px 0;">
            <tr><td align="center">
              <table width="520" cellpadding="0" cellspacing="0" border="0"
                     style="background:#FFFFFF;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(11,27,46,0.08);">
                <!-- Header -->
                <tr>
                  <td style="background:#0B1B2E;padding:32px 40px;">
                    <span style="font-family:Georgia,serif;font-style:italic;font-size:26px;color:#FFFFFF;letter-spacing:-0.02em;">
                      Forge<span style="display:inline-block;width:5px;height:5px;background:#B8956A;border-radius:50%;margin-left:3px;vertical-align:super;"></span>
                    </span>
                  </td>
                </tr>
                <!-- Body -->
                <tr>
                  <td style="padding:40px 40px 32px;">
                    <h1 style="font-family:Georgia,serif;font-weight:400;font-size:32px;line-height:1.1;letter-spacing:-0.02em;color:#0B1B2E;margin:0 0 12px;">%s</h1>
                    <p style="font-size:15px;color:#364658;line-height:1.55;margin:0 0 36px;">%s<br/>This code expires in <strong>10 minutes</strong>.</p>
                    <!-- OTP CODE -->
                    <table cellpadding="0" cellspacing="0" border="0" style="margin:0 auto 36px;">
                      <tr>
                        %s
                      </tr>
                    </table>
                    <p style="font-size:13px;color:#8E99A6;line-height:1.55;margin:0;">
                      If you didn't request this code, you can safely ignore this email.<br/>
                      For help, contact <a href="mailto:support@forge.al" style="color:#8C6E47;">support@forge.al</a>
                    </p>
                  </td>
                </tr>
                <!-- Footer -->
                <tr>
                  <td style="padding:20px 40px 28px;border-top:1px solid #ECEEF1;">
                    <p style="font-size:12px;color:#B3BCC6;margin:0;text-align:center;">
                      © 2026 Forge · Made in Tiranë, Albania
                    </p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(heading, heading, subtext, buildCodeCells(code));
    }

    /** Build individual digit boxes for the OTP code */
    private String buildCodeCells(String code) {
        StringBuilder sb = new StringBuilder();
        for (char c : code.toCharArray()) {
            sb.append("""
                <td style="padding:0 6px;">
                  <div style="width:52px;height:64px;background:#FAFAF7;border:1.5px solid #D8DCE2;
                              border-radius:8px;display:flex;align-items:center;justify-content:center;
                              font-family:Georgia,serif;font-size:32px;font-weight:400;
                              color:#0B1B2E;text-align:center;line-height:64px;">%c</div>
                </td>
            """.formatted(c));
        }
        return sb.toString();
    }
}
