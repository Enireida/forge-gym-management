package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.User;
import com.unyt.gym.entity.WorkoutLog;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.UserRepository;
import com.unyt.gym.repository.WorkoutLogRepository;
import com.unyt.gym.service.StravaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Handles Strava OAuth2 connect / callback / sync for members.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /client/strava/connect  — redirect to Strava authorization page</li>
 *   <li>GET  /client/strava/callback — handle OAuth2 callback from Strava</li>
 *   <li>POST /client/strava/sync     — pull latest activities and save to WorkoutLog</li>
 *   <li>POST /client/strava/disconnect — remove stored tokens</li>
 * </ul>
 */
@Controller
@RequestMapping("/client/strava")
public class StravaController {

    private static final Logger log = LoggerFactory.getLogger(StravaController.class);

    private final StravaService       stravaService;
    private final UserRepository      userRepo;
    private final MemberRepository    memberRepo;
    private final WorkoutLogRepository workoutLogRepo;

    public StravaController(StravaService stravaService,
                            UserRepository userRepo,
                            MemberRepository memberRepo,
                            WorkoutLogRepository workoutLogRepo) {
        this.stravaService  = stravaService;
        this.userRepo       = userRepo;
        this.memberRepo     = memberRepo;
        this.workoutLogRepo = workoutLogRepo;
    }

    /** Redirects the member to Strava's authorization page. */
    @GetMapping("/connect")
    public String connect(HttpSession session) {
        if (session.getAttribute("currentUser") == null) return "redirect:/login";
        return "redirect:" + stravaService.getAuthorizationUrl();
    }

    /**
     * OAuth2 callback — Strava redirects here after the user approves access.
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
            ra.addFlashAttribute("stravaError", "Strava access was denied.");
            return "redirect:/client/workouts";
        }
        if (code == null) {
            ra.addFlashAttribute("stravaError", "No authorization code received from Strava.");
            return "redirect:/client/workouts";
        }

        // Fetch from DB to get a managed entity
        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(user);
        Long athleteId = stravaService.exchangeCode(code, dbUser);

        if (athleteId != null) {
            // Update session copy
            user.setStravaAccessToken(dbUser.getStravaAccessToken());
            user.setStravaRefreshToken(dbUser.getStravaRefreshToken());
            user.setStravaTokenExpiry(dbUser.getStravaTokenExpiry());
            user.setStravaAthleteId(athleteId);
            session.setAttribute("currentUser", user);
            ra.addFlashAttribute("stravaSuccess",
                "Strava connected! Click \"Sync from Strava\" to import your activities.");
        } else {
            ra.addFlashAttribute("stravaError", "Failed to connect Strava. Please try again.");
        }
        return "redirect:/client/workouts";
    }

    /**
     * Syncs the last 30 activities from Strava into the member's WorkoutLog.
     * Activities already imported (same Strava ID) are skipped via healthKitUUID dedup.
     */
    @PostMapping("/sync")
    public String sync(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";
        if (!user.isStravaConnected()) {
            ra.addFlashAttribute("stravaError", "Strava is not connected.");
            return "redirect:/client/workouts";
        }

        Member member = memberRepo.findByEmail(user.getEmail()).orElse(null);
        if (member == null) {
            ra.addFlashAttribute("stravaError", "Member profile not found.");
            return "redirect:/client/workouts";
        }

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(user);
        List<WorkoutLog> activities = stravaService.fetchActivities(dbUser, member, 30);

        int imported = 0;
        for (WorkoutLog w : activities) {
            // Dedup: skip if already imported (same Strava activity ID)
            if (w.getHealthKitUUID() != null &&
                    workoutLogRepo.existsByHealthKitUUID(w.getHealthKitUUID())) {
                continue;
            }
            workoutLogRepo.save(w);
            imported++;
        }

        // Refresh session tokens if they were updated during refresh
        user.setStravaAccessToken(dbUser.getStravaAccessToken());
        user.setStravaTokenExpiry(dbUser.getStravaTokenExpiry());
        session.setAttribute("currentUser", user);

        if (imported > 0) {
            ra.addFlashAttribute("stravaSuccess",
                "Synced " + imported + " new activit" + (imported == 1 ? "y" : "ies") + " from Strava!");
        } else {
            ra.addFlashAttribute("stravaInfo", "No new activities to import — already up to date.");
        }
        log.info("[STRAVA] Synced {} new activities for {}", imported, user.getEmail());
        return "redirect:/client/workouts";
    }

    /** Removes the stored Strava tokens — disconnects the integration. */
    @PostMapping("/disconnect")
    public String disconnect(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(null);
        if (dbUser != null) {
            dbUser.setStravaAccessToken(null);
            dbUser.setStravaRefreshToken(null);
            dbUser.setStravaTokenExpiry(null);
            dbUser.setStravaAthleteId(null);
            userRepo.save(dbUser);
        }
        user.setStravaAccessToken(null);
        user.setStravaAthleteId(null);
        session.setAttribute("currentUser", user);

        ra.addFlashAttribute("stravaInfo", "Strava disconnected.");
        return "redirect:/client/workouts";
    }
}
