package com.unyt.gym.controller.web;

import com.unyt.gym.entity.VerificationToken.TokenType;
import com.unyt.gym.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles email OTP verification flow:
 *
 *  GET  /verify/email          → show OTP entry form
 *  POST /verify/email/send     → send OTP to email (stored in session)
 *  POST /verify/email/confirm  → verify entered code
 *  GET  /verify/email/resend   → resend OTP
 */
@Controller
@RequestMapping("/verify")
public class VerificationController {

    private static final Logger log = LoggerFactory.getLogger(VerificationController.class);
    private final OtpService otpService;

    public VerificationController(OtpService otpService) {
        this.otpService = otpService;
    }

    // ─────────────────────────────────────────────
    //  SHOW OTP PAGE
    // ─────────────────────────────────────────────

    @GetMapping("/email")
    public String showVerifyPage(HttpSession session, Model model,
                                  @RequestParam(required = false) String email) {
        // Email can come from session (post-registration) or query param
        String targetEmail = email != null ? email : (String) session.getAttribute("pendingVerifyEmail");

        if (targetEmail == null) {
            return "redirect:/login";
        }

        session.setAttribute("pendingVerifyEmail", targetEmail);
        model.addAttribute("email", targetEmail);
        model.addAttribute("masked", maskEmail(targetEmail));
        return "verify-email";
    }

    // ─────────────────────────────────────────────
    //  SEND / RESEND OTP
    // ─────────────────────────────────────────────

    @PostMapping("/email/send")
    public String sendOtp(HttpSession session, RedirectAttributes ra,
                          @RequestParam String email) {
        session.setAttribute("pendingVerifyEmail", email);
        try {
            otpService.sendOtp(email, TokenType.EMAIL_VERIFY);
            ra.addFlashAttribute("info", "A 6-digit code was sent to " + maskEmail(email));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not send code. Please try again.");
        }
        return "redirect:/verify/email?email=" + email;
    }

    @GetMapping("/email/resend")
    public String resendOtp(HttpSession session, RedirectAttributes ra) {
        String email = (String) session.getAttribute("pendingVerifyEmail");
        if (email == null) return "redirect:/login";
        try {
            otpService.sendOtp(email, TokenType.EMAIL_VERIFY);
            ra.addFlashAttribute("info", "New code sent to " + maskEmail(email));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not resend code.");
        }
        return "redirect:/verify/email";
    }

    // ─────────────────────────────────────────────
    //  CONFIRM OTP
    // ─────────────────────────────────────────────

    @PostMapping("/email/confirm")
    public String confirmOtp(@RequestParam String code,
                              HttpSession session,
                              RedirectAttributes ra) {
        String email = (String) session.getAttribute("pendingVerifyEmail");
        if (email == null) return "redirect:/login";

        // Combine digits from 6 separate inputs or single field
        String cleanCode = code.replaceAll("\\s+", "").trim();

        boolean ok = otpService.verifyOtp(email, cleanCode, TokenType.EMAIL_VERIFY);
        if (ok) {
            session.removeAttribute("pendingVerifyEmail");
            session.setAttribute("emailVerified", email);
            ra.addFlashAttribute("success", "Email verified! You can now sign in.");
            return "redirect:/login";
        } else {
            ra.addFlashAttribute("error", "Invalid or expired code. Please try again.");
            return "redirect:/verify/email";
        }
    }

    // ─────────────────────────────────────────────
    //  2FA LOGIN OTP
    // ─────────────────────────────────────────────

    @GetMapping("/login-otp")
    public String showLoginOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("pendingLoginEmail");
        if (email == null) return "redirect:/login";
        model.addAttribute("masked", maskEmail(email));
        return "verify-login-otp";
    }

    @PostMapping("/login-otp/confirm")
    public String confirmLoginOtp(@RequestParam String code,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        String email = (String) session.getAttribute("pendingLoginEmail");
        if (email == null) return "redirect:/login";

        String cleanCode = code.replaceAll("\\s+", "").trim();
        boolean ok = otpService.verifyOtp(email, cleanCode, TokenType.LOGIN_2FA);

        if (ok) {
            session.removeAttribute("pendingLoginEmail");
            session.setAttribute("twoFaVerified", true);
            return "redirect:/"; // Spring Security takes over from here
        } else {
            ra.addFlashAttribute("error", "Invalid or expired code. Please try again.");
            return "redirect:/verify/login-otp";
        }
    }

    // ─────────────────────────────────────────────
    //  HELPER
    // ─────────────────────────────────────────────

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String local = parts[0];
        String shown = local.length() <= 2
            ? local
            : local.substring(0, 2) + "•".repeat(Math.min(local.length() - 2, 4));
        return shown + "@" + parts[1];
    }
}
