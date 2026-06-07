package com.unyt.gym.controller.web;

import com.unyt.gym.entity.*;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.MembershipPlanRepository;
import com.unyt.gym.repository.UserRepository;
import com.unyt.gym.service.AuthService;
import com.unyt.gym.service.EmailService;
import com.unyt.gym.service.MemberService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;

/**
 * Public pages: landing, login, register, logout.
 */
@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final AuthService              authService;
    private final MembershipPlanRepository planRepository;
    private final UserRepository           userRepository;
    private final MemberRepository         memberRepository;
    private final MemberService            memberService;
    private final PasswordEncoder          passwordEncoder;
    private final EmailService             emailService;

    @org.springframework.beans.factory.annotation.Value("${forge.mail.dev-mode:false}")
    private boolean devMode;

    public HomeController(AuthService authService,
                          MembershipPlanRepository planRepository,
                          UserRepository userRepository,
                          MemberRepository memberRepository,
                          MemberService memberService,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.authService      = authService;
        this.planRepository   = planRepository;
        this.userRepository   = userRepository;
        this.memberRepository = memberRepository;
        this.memberService    = memberService;
        this.passwordEncoder  = passwordEncoder;
        this.emailService     = emailService;
    }

    /* ── Landing ─────────────────────────────────────────────── */

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("plans", planRepository.findByActiveTrue());
        return "landing";
    }

    /* ── Demo (illustrative walkthrough of the whole product) ── */

    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }

    /**
     * Browsers automatically request /favicon.ico on every page load.
     * We don't have an .ico file, so we redirect to the SVG version
     * to prevent 404 errors flooding the logs.
     */
    @GetMapping("/favicon.ico")
    public String favicon() {
        return "redirect:/favicon.svg";
    }

    /* ── Login ───────────────────────────────────────────────── */

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = authService.requireAuthenticated(email, password);
            if (user.getRole() == Role.CLIENT) {
                Member member = memberRepository.findByEmail(user.getEmail().trim().toLowerCase()).orElse(null);
                if (member != null && member.getStatus() == MemberStatus.PENDING) {
                    model.addAttribute("error", "Llogaria juaj pret konfirmimin nga recepsioni. Do të njoftoheni me email sapo të aktivizohet.");
                    return "login";
                }
            }
            session.setAttribute("currentUser", user);
            return switch (user.getRole()) {
                case ADMIN, MANAGER -> "redirect:/admin";
                case TRAINER        -> "redirect:/trainer";
                case CLIENT         -> "redirect:/client";
            };
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }

    /* ── Register ────────────────────────────────────────────── */

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("plans", planRepository.findByActiveTrue());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String birthDate,
                           @RequestParam(required = false) String gender,
                           @RequestParam(required = false) Long planId,
                           HttpSession session,
                           Model model) {

        List<MembershipPlan> plans = planRepository.findByActiveTrue();

        if (planId == null) {
            // Auto-select first available plan if none chosen
            if (!plans.isEmpty()) {
                planId = plans.get(0).getId();
            } else {
                model.addAttribute("error", "No membership plans available. Please contact reception.");
                model.addAttribute("plans", plans);
                return "register";
            }
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("plans", plans);
            return "register";
        }

        // Validate password length
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            model.addAttribute("plans", plans);
            return "register";
        }

        String cleanEmail = email.trim().toLowerCase();

        // Check for existing account
        if (userRepository.existsByEmail(cleanEmail)) {
            model.addAttribute("error", "An account with this email already exists. Please log in.");
            model.addAttribute("plans", plans);
            return "register";
        }

        try {
            // Generate 2-digit verification code + 2 decoys
            SecureRandom rng = new SecureRandom();
            int correctCode = 10 + rng.nextInt(90); // 10–99
            int decoy1, decoy2;
            do { decoy1 = 10 + rng.nextInt(90); } while (decoy1 == correctCode);
            do { decoy2 = 10 + rng.nextInt(90); } while (decoy2 == correctCode || decoy2 == decoy1);

            List<Integer> choices = new ArrayList<>(List.of(correctCode, decoy1, decoy2));
            Collections.shuffle(choices);

            // Send verification email FIRST — only proceed if it actually goes out
            String html = "<div style='font-family:sans-serif;max-width:480px;margin:auto'>"
                + "<h2 style='color:#1a1a2e'>Verify your Forge account</h2>"
                + "<p>Hi " + firstName.trim() + ",</p>"
                + "<p>You are one step away from joining Forge. "
                + "On the next page you will see <strong>three numbers</strong>. "
                + "Click the one that matches the number below:</p>"
                + "<div style='text-align:center;margin:32px 0'>"
                + "<span style='font-size:64px;font-weight:700;color:#B8956A;letter-spacing:4px'>"
                + correctCode + "</span></div>"
                + "<p style='color:#888;font-size:13px'>This code expires in 10 minutes. "
                + "If you did not request this, ignore this email.</p>"
                + "</div>";

            boolean sent = emailService.send(cleanEmail, "Your Forge verification code", html);
            if (!sent) {
                model.addAttribute("error",
                    "We could not send the verification email to " + cleanEmail +
                    ". Please check the address is correct and try again.");
                model.addAttribute("plans", plans);
                return "register";
            }

            // Email sent — now save registration data in session and go to verify page
            Map<String, Object> regData = new HashMap<>();
            regData.put("firstName",  firstName.trim());
            regData.put("lastName",   lastName.trim());
            regData.put("email",      cleanEmail);
            regData.put("password",   passwordEncoder.encode(password));
            regData.put("phone",      phone);
            regData.put("birthDate",  birthDate);
            regData.put("gender",     gender);
            regData.put("planId",     planId);
            regData.put("code",       correctCode);
            regData.put("choices",    choices);
            regData.put("attempts",   0);
            regData.put("expires",    LocalDateTime.now().plusMinutes(10));
            session.setAttribute("pendingReg", regData);

            return "redirect:/register/verify";

        } catch (Exception ex) {
            log.error("[REGISTER] Failed for {}: {}", cleanEmail, ex.getMessage(), ex);
            model.addAttribute("error", "Registration failed. Please try again.");
            model.addAttribute("plans", plans);
            return "register";
        }
    }

    @GetMapping("/register/verify")
    public String registerVerifyForm(HttpSession session, Model model) {
        Map<?, ?> reg = (Map<?, ?>) session.getAttribute("pendingReg");
        if (reg == null) return "redirect:/register";
        model.addAttribute("choices", reg.get("choices"));
        model.addAttribute("email",   reg.get("email"));
        return "register-verify";
    }

    @PostMapping("/register/verify")
    public String registerVerify(@RequestParam int selected,
                                 HttpSession session,
                                 Model model) {
        @SuppressWarnings("unchecked")
        Map<String, Object> reg = (Map<String, Object>) session.getAttribute("pendingReg");
        if (reg == null) return "redirect:/register";

        // Check expiry
        if (LocalDateTime.now().isAfter((LocalDateTime) reg.get("expires"))) {
            session.removeAttribute("pendingReg");
            model.addAttribute("error", "Verification expired. Please register again.");
            model.addAttribute("plans", planRepository.findByActiveTrue());
            return "register";
        }

        int attempts = (int) reg.get("attempts") + 1;
        reg.put("attempts", attempts);

        if (selected != (int) reg.get("code")) {
            if (attempts >= 3) {
                session.removeAttribute("pendingReg");
                model.addAttribute("error", "Too many wrong attempts. Please register again.");
                model.addAttribute("plans", planRepository.findByActiveTrue());
                return "register";
            }
            model.addAttribute("choices", reg.get("choices"));
            model.addAttribute("email",   reg.get("email"));
            model.addAttribute("error",   "Wrong number. Try again (" + (3 - attempts) + " attempts left).");
            return "register-verify";
        }

        // ── Code correct → create account ──────────────────────────────
        try {
            Long planId = (Long) reg.get("planId");
            MembershipPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found."));

            String fullName = reg.get("firstName") + " " + reg.get("lastName");
            User user = new User(fullName, (String) reg.get("email"),
                    (String) reg.get("password"), Role.CLIENT);
            // password is already encoded
            user.setPassword((String) reg.get("password"));
            User savedUser = userRepository.save(user);

            Member member = new Member();
            member.setName((String) reg.get("firstName"));
            member.setSurname((String) reg.get("lastName"));
            member.setEmail((String) reg.get("email"));
            String phone = (String) reg.get("phone");
            member.setPhone(phone != null && !phone.isBlank() ? phone.trim() : null);
            String birthDate = (String) reg.get("birthDate");
            if (birthDate != null && !birthDate.isBlank()) {
                try { member.setBirthDate(LocalDate.parse(birthDate)); } catch (Exception ignored) {}
            }
            String gender = (String) reg.get("gender");
            if (gender != null && !gender.isBlank()) member.setGender(gender.trim());
            member.setPlan(plan);
            member.setUser(savedUser);
            member.setStatus(MemberStatus.PENDING);
            memberService.create(member);

            session.removeAttribute("pendingReg");
            return "redirect:/register/pending";

        } catch (Exception ex) {
            model.addAttribute("error", "Account creation failed: " + ex.getMessage());
            model.addAttribute("plans", planRepository.findByActiveTrue());
            return "register";
        }
    }

    @GetMapping("/register/pending")
    public String registerPending() {
        return "register-pending";
    }

    /* ── Contact / Enquiry ───────────────────────────────────── */

    @PostMapping("/contact")
    public String contact(@RequestParam String contactName,
                          @RequestParam(required = false) String contactEmail,
                          @RequestParam(required = false) String contactPhone,
                          @RequestParam String contactMessage,
                          org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            String html = "<div style='font-family:sans-serif;max-width:560px'>"
                + "<h2 style='color:#1a1a2e'>New Enquiry from Forge Landing Page</h2>"
                + "<p><b>Name:</b> " + contactName + "</p>"
                + "<p><b>Email:</b> " + (contactEmail != null ? contactEmail : "—") + "</p>"
                + "<p><b>Phone:</b> " + (contactPhone != null ? contactPhone : "—") + "</p>"
                + "<p><b>Message:</b></p><p style='background:#f5f5f5;padding:12px;border-radius:8px'>"
                + contactMessage.replace("\n", "<br>") + "</p></div>";
            emailService.send("recienireida@gmail.com", "Forge Enquiry — " + contactName, html);
            ra.addFlashAttribute("contactSuccess", "Message sent! We will get back to you soon.");
        } catch (Exception e) {
            ra.addFlashAttribute("contactError", "Could not send message. Please try again.");
        }
        return "redirect:/#contact-form";
    }

    /* ── Forgot password ─────────────────────────────────────── */

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    /**
     * Generates a 6-digit OTP, saves it to the user record with a 15-minute expiry,
     * and sends it by email. Always shows the same success page regardless of whether
     * the email was found (prevents user enumeration).
     */
    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email,
                                       @RequestParam(defaultValue = "SQ") String lang,
                                       RedirectAttributes ra) {
        String clean = email.trim().toLowerCase();
        log.debug("[FORGOT-PWD] Password reset requested for: {}", clean);
        String[] generatedOtp = {null};
        boolean[] sendFailed = {false};
        userRepository.findByEmail(clean).ifPresent(user -> {
            String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            user.setResetToken(otp);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            boolean sent = emailService.sendPasswordResetOtp(user.getEmail(), user.getName(), otp, lang);
            if (sent) {
                log.info("[FORGOT-PWD] OTP email sent to {}", user.getEmail());
                generatedOtp[0] = otp;
            } else {
                log.error("[FORGOT-PWD] OTP email FAILED to send to {}", user.getEmail());
                sendFailed[0] = true;
            }
        });
        if (generatedOtp[0] == null && !sendFailed[0]) {
            log.debug("[FORGOT-PWD] No user found for email: {}", clean);
        }

        // If the email exists but the message could not be sent, tell the user to retry
        if (sendFailed[0]) {
            boolean en = "EN".equalsIgnoreCase(lang);
            ra.addFlashAttribute("error", en
                ? "We could not send the reset code right now. Please try again in a moment."
                : "Nuk mundëm të dërgojmë kodin tani. Ju lutemi provoni sërisht pas pak.");
            return "redirect:/forgot-password";
        }

        ra.addFlashAttribute("email", clean);
        ra.addFlashAttribute("otpSent", true);
        // In dev-mode show the OTP directly on screen so no email setup is needed
        if (devMode && generatedOtp[0] != null) {
            ra.addFlashAttribute("devOtp", generatedOtp[0]);
        }
        return "redirect:/forgot-password/verify";
    }

    @GetMapping("/forgot-password/verify")
    public String verifyOtpForm(Model model) {
        return "forgot-password-verify";
    }

    /**
     * Checks the OTP (case/whitespace-safe), enforces expiry,
     * sets the new BCrypt-hashed password, clears the token.
     */
    @PostMapping("/forgot-password/verify")
    public String verifyOtpSubmit(@RequestParam String email,
                                  @RequestParam String otp,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  RedirectAttributes ra) {
        String clean = email.trim().toLowerCase();

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Fjalekalimi i ri nuk perputhet.");
            ra.addFlashAttribute("email", clean);
            return "redirect:/forgot-password/verify";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Fjalekalimi duhet te kete te pakten 6 karaktere.");
            ra.addFlashAttribute("email", clean);
            return "redirect:/forgot-password/verify";
        }

        User user = userRepository.findByEmail(clean).orElse(null);
        if (user == null || user.getResetToken() == null) {
            ra.addFlashAttribute("error", "Kodi eshte i pavlefshëm. Provoni perseri.");
            return "redirect:/forgot-password";
        }
        if (!user.getResetToken().equals(otp.trim())) {
            ra.addFlashAttribute("error", "Kodi i gabuar. Kontrolloni emailin tuaj.");
            ra.addFlashAttribute("email", clean);
            return "redirect:/forgot-password/verify";
        }
        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            ra.addFlashAttribute("error", "Kodi ka skaduar. Kerkoni nje kod te ri.");
            return "redirect:/forgot-password";
        }

        // All good — update password and clear OTP
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        ra.addFlashAttribute("success", "Fjalekalimi u ndryshua me sukses. Identifikohuni tani.");
        return "redirect:/login";
    }

    /** AJAX endpoint — validates OTP before the client shows the password fields. */
    @GetMapping("/forgot-password/check-otp")
    @ResponseBody
    public Map<String, Object> checkOtp(@RequestParam String email,
                                        @RequestParam String otp) {
        String clean = email.trim().toLowerCase();
        User user = userRepository.findByEmail(clean).orElse(null);
        if (user == null || user.getResetToken() == null) {
            return Map.of("valid", false, "message", "Kodi është i pavlefshëm. Provoni përsëri.");
        }
        if (!user.getResetToken().equals(otp.trim())) {
            return Map.of("valid", false, "message", "Kod i gabuar. Kontrolloni emailin tuaj.");
        }
        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            return Map.of("valid", false, "message", "Kodi ka skaduar. Kërkoni një kod të ri.");
        }
        return Map.of("valid", true);
    }

    /* ── Logout ──────────────────────────────────────────────── */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * Quick SMTP diagnostic — visit /test-smtp in the browser to see if email works.
     * Remove this endpoint before going to production.
     */
    @GetMapping("/test-smtp")
    @ResponseBody
    public String testSmtp(@org.springframework.beans.factory.annotation.Value("${spring.mail.username:NOT_SET}") String username,
                           @org.springframework.beans.factory.annotation.Value("${spring.mail.password:NOT_SET}") String password,
                           @org.springframework.beans.factory.annotation.Value("${forge.mail.from:NOT_SET}") String from,
                           @org.springframework.beans.factory.annotation.Value("${forge.mail.dev-mode:false}") boolean devMode) {
        StringBuilder sb = new StringBuilder("<pre style='font-family:monospace;font-size:14px;padding:20px'>");
        sb.append("<b>SMTP Config:</b>\n");
        sb.append("  username  = ").append(username).append("\n");
        sb.append("  password  = ").append(password.length() > 4 ? password.substring(0,4) + "****" : "NOT_SET").append("\n");
        sb.append("  from      = ").append(from).append("\n");
        sb.append("  dev-mode  = ").append(devMode).append("\n\n");

        if (devMode) {
            sb.append("<span style='color:orange'>⚠ dev-mode=true — emails are NOT sent. Set forge.mail.dev-mode=false to send real emails.</span>\n");
        } else {
            sb.append("<b>Attempting to send test email to ").append(username).append("...</b>\n");
            try {
                boolean sent = emailService.send(username,
                        "Forge SMTP Test",
                        "<p>SMTP is working! This is a test from Forge.</p>");
                if (sent) {
                    sb.append("<span style='color:green'>✅ Email sent successfully! Check ").append(username).append("</span>\n");
                } else {
                    sb.append("<span style='color:red'>❌ send() returned false — check IntelliJ console for [EMAIL] Failed log line.</span>\n");
                }
            } catch (Exception e) {
                sb.append("<span style='color:red'>❌ Exception: ").append(e.getMessage()).append("</span>\n");
            }
        }
        sb.append("</pre>");
        return sb.toString();
    }
}
