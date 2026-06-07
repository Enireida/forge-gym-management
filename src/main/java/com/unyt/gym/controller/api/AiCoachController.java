package com.unyt.gym.controller.api;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.Role;
import com.unyt.gym.entity.Staff;
import com.unyt.gym.entity.User;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.StaffRepository;
import com.unyt.gym.service.AiCoachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST endpoint for the Forge AI Fitness Coach.
 *
 * POST /api/ai/coach — { "message": "..." }
 *                    → { "reply": "...", "timestamp": "..." }
 *
 * Role logic:
 *  CLIENT  → member-aware personalised responses + privacy guard
 *  TRAINER → can ask about any of their assigned clients by name + general coaching
 *  ADMIN   → general coaching assistant
 */
@RestController
@RequestMapping("/api/ai")
public class AiCoachController {

    private static final Logger log = LoggerFactory.getLogger(AiCoachController.class);

    private final AiCoachService    aiCoachService;
    private final MemberRepository  memberRepo;
    private final StaffRepository   staffRepo;

    public AiCoachController(AiCoachService aiCoachService,
                             MemberRepository memberRepo,
                             StaffRepository staffRepo) {
        this.aiCoachService = aiCoachService;
        this.memberRepo     = memberRepo;
        this.staffRepo      = staffRepo;
    }

    @PostMapping("/coach")
    public ResponseEntity<?> chat(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        User user = (User) session.getAttribute("currentUser");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Message is empty"));
        if (message.length() > 600)
            return ResponseEntity.badRequest().body(Map.of("error", "Message too long (max 600 chars)"));

        String reply;

        if (user.getRole() == Role.CLIENT) {
            Member member = memberRepo.findByEmail(user.getEmail()).orElse(null);
            if (member == null)
                return ResponseEntity.status(404).body(Map.of("error", "No member profile linked to this account"));
            reply = aiCoachService.respondForClient(member, message);

        } else if (user.getRole() == Role.TRAINER) {
            Staff trainer = staffRepo.findByEmail(user.getEmail()).orElse(null);
            if (trainer == null) {
                // Fallback — trainer profile not found, treat as general
                reply = "I couldn't find your trainer profile. Please contact an admin to link your account to a staff record.";
            } else {
                reply = aiCoachService.respondForTrainer(trainer, message);
            }
        } else {
            // ADMIN — general coaching assistant
            reply = aiCoachService.respondForTrainer(
                buildFakeStaff(user.getName(), user.getEmail()), message);
        }

        log.debug("AI Coach: role={} user={} reply_len={}", user.getRole(), user.getEmail(), reply.length());

        return ResponseEntity.ok(Map.of(
            "reply",     reply,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /** Creates a transient Staff-like object for admin users who ask the AI. */
    private Staff buildFakeStaff(String name, String email) {
        Staff s = new Staff();
        s.setName(name != null ? name : "Admin");
        s.setEmail(email);
        s.setRole("Admin");
        return s;
    }
}
