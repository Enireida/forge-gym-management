package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.User;
import com.unyt.gym.entity.WorkoutLog;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.UserRepository;
import com.unyt.gym.repository.WorkoutLogRepository;
import com.unyt.gym.service.GoogleFitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Handles Google Fit OAuth2 connect / callback / sync / disconnect for members.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /client/googlefit/connect    — redirect to Google authorization page</li>
 *   <li>GET  /client/googlefit/callback   — handle OAuth2 callback from Google</li>
 *   <li>POST /client/googlefit/sync       — pull latest sessions and save to WorkoutLog</li>
 *   <li>POST /client/googlefit/disconnect — remove stored tokens</li>
 * </ul>
 */
@Controller
@RequestMapping("/client/googlefit")
public class GoogleFitController {

    private static final Logger log = LoggerFactory.getLogger(GoogleFitController.class);

    private final GoogleFitService     googleFitService;
    private final UserRepository       userRepo;
    private final MemberRepository     memberRepo;
    private final WorkoutLogRepository workoutLogRepo;

    public GoogleFitController(GoogleFitService googleFitService,
                               UserRepository userRepo,
                               MemberRepository memberRepo,
                               WorkoutLogRepository workoutLogRepo) {
        this.googleFitService = googleFitService;
        this.userRepo         = userRepo;
        this.memberRepo       = memberRepo;
        this.workoutLogRepo   = workoutLogRepo;
    }

    /** Redirects the member to Google's authorization page. */
    @GetMapping("/connect")
    public String connect(HttpSession session) {
        if (session.getAttribute("currentUser") == null) return "redirect:/login";
        return "redirect:" + googleFitService.getAuthorizationUrl();
    }

    /**
     * OAuth2 callback — Google redirects here after the user approves access.
     * Exchanges the code for tokens and saves them to the User entity.
     */
    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String error,
                           HttpSession session,
                           RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        if (error != null) {
            ra.addFlashAttribute("googleFitError", "Google Fit access was denied.");
            return "redirect:/client/workouts";
        }
        if (code == null) {
            ra.addFlashAttribute("googleFitError", "No authorization code received from Google.");
            return "redirect:/client/workouts";
        }

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(user);
        String googleEmail = googleFitService.exchangeCode(code, dbUser);

        if (googleEmail != null) {
            user.setGoogleFitAccessToken(dbUser.getGoogleFitAccessToken());
            user.setGoogleFitRefreshToken(dbUser.getGoogleFitRefreshToken());
            user.setGoogleFitTokenExpiry(dbUser.getGoogleFitTokenExpiry());
            user.setGoogleFitEmail(googleEmail);
            session.setAttribute("currentUser", user);
            ra.addFlashAttribute("googleFitSuccess",
                "Google Fit connected! Click \"Sync from Google Fit\" to import your activities.");
        } else {
            ra.addFlashAttribute("googleFitError",
                "Failed to connect Google Fit. Please try again.");
        }
        return "redirect:/client/workouts";
    }

    /**
     * Syncs the last 20 sessions (within 30 days) from Google Fit into the member's WorkoutLog.
     * Sessions already imported are skipped via healthKitUUID dedup.
     */
    @PostMapping("/sync")
    public String sync(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";
        if (!user.isGoogleFitConnected()) {
            ra.addFlashAttribute("googleFitError", "Google Fit is not connected.");
            return "redirect:/client/workouts";
        }

        Member member = memberRepo.findByEmail(user.getEmail()).orElse(null);
        if (member == null) {
            ra.addFlashAttribute("googleFitError", "Member profile not found.");
            return "redirect:/client/workouts";
        }

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(user);
        List<WorkoutLog> activities = googleFitService.fetchActivities(dbUser, member, 20);

        int imported = 0;
        for (WorkoutLog w : activities) {
            if (w.getHealthKitUUID() != null &&
                    workoutLogRepo.existsByHealthKitUUID(w.getHealthKitUUID())) {
                continue;
            }
            workoutLogRepo.save(w);
            imported++;
        }

        user.setGoogleFitAccessToken(dbUser.getGoogleFitAccessToken());
        user.setGoogleFitTokenExpiry(dbUser.getGoogleFitTokenExpiry());
        session.setAttribute("currentUser", user);

        if (imported > 0) {
            ra.addFlashAttribute("googleFitSuccess",
                "Synced " + imported + " new activit" + (imported == 1 ? "y" : "ies") + " from Google Fit!");
        } else {
            ra.addFlashAttribute("googleFitInfo",
                "No new Google Fit activities to import — already up to date.");
        }
        log.info("[GOOGLEFIT] Synced {} new activities for {}", imported, user.getEmail());
        return "redirect:/client/workouts";
    }

    /** Removes the stored Google Fit tokens — disconnects the integration. */
    @PostMapping("/disconnect")
    public String disconnect(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(null);
        if (dbUser != null) {
            dbUser.setGoogleFitAccessToken(null);
            dbUser.setGoogleFitRefreshToken(null);
            dbUser.setGoogleFitTokenExpiry(null);
            dbUser.setGoogleFitEmail(null);
            userRepo.save(dbUser);
        }
        user.setGoogleFitAccessToken(null);
        user.setGoogleFitEmail(null);
        session.setAttribute("currentUser", user);

        ra.addFlashAttribute("googleFitInfo", "Google Fit disconnected.");
        return "redirect:/client/workouts";
    }
}
