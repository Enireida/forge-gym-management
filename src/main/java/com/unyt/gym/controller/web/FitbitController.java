package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Member;
import com.unyt.gym.entity.User;
import com.unyt.gym.entity.WorkoutLog;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.UserRepository;
import com.unyt.gym.repository.WorkoutLogRepository;
import com.unyt.gym.service.FitbitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Handles Fitbit OAuth2 connect / callback / sync / disconnect for members.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /client/fitbit/connect    — redirect to Fitbit authorization page</li>
 *   <li>GET  /client/fitbit/callback   — handle OAuth2 callback from Fitbit</li>
 *   <li>POST /client/fitbit/sync       — pull latest activities and save to WorkoutLog</li>
 *   <li>POST /client/fitbit/disconnect — remove stored tokens</li>
 * </ul>
 */
@Controller
@RequestMapping("/client/fitbit")
public class FitbitController {

    private static final Logger log = LoggerFactory.getLogger(FitbitController.class);

    private final FitbitService        fitbitService;
    private final UserRepository       userRepo;
    private final MemberRepository     memberRepo;
    private final WorkoutLogRepository workoutLogRepo;

    public FitbitController(FitbitService fitbitService,
                            UserRepository userRepo,
                            MemberRepository memberRepo,
                            WorkoutLogRepository workoutLogRepo) {
        this.fitbitService  = fitbitService;
        this.userRepo       = userRepo;
        this.memberRepo     = memberRepo;
        this.workoutLogRepo = workoutLogRepo;
    }

    /** Redirects the member to Fitbit's authorization page. */
    @GetMapping("/connect")
    public String connect(HttpSession session) {
        if (session.getAttribute("currentUser") == null) return "redirect:/login";
        return "redirect:" + fitbitService.getAuthorizationUrl();
    }

    /**
     * OAuth2 callback — Fitbit redirects here after the user approves access.
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
            ra.addFlashAttribute("fitbitError", "Fitbit access was denied.");
            return "redirect:/client/workouts";
        }
        if (code == null) {
            ra.addFlashAttribute("fitbitError", "No authorization code received from Fitbit.");
            return "redirect:/client/workouts";
        }

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(user);
        String fitbitUserId = fitbitService.exchangeCode(code, dbUser);

        if (fitbitUserId != null) {
            user.setFitbitAccessToken(dbUser.getFitbitAccessToken());
            user.setFitbitRefreshToken(dbUser.getFitbitRefreshToken());
            user.setFitbitTokenExpiry(dbUser.getFitbitTokenExpiry());
            user.setFitbitUserId(fitbitUserId);
            session.setAttribute("currentUser", user);
            ra.addFlashAttribute("fitbitSuccess",
                "Fitbit connected! Click \"Sync from Fitbit\" to import your activities.");
        } else {
            ra.addFlashAttribute("fitbitError", "Failed to connect Fitbit. Please try again.");
        }
        return "redirect:/client/workouts";
    }

    /**
     * Syncs the last 20 activities (within 30 days) from Fitbit into the member's WorkoutLog.
     * Activities already imported (same Fitbit logId) are skipped via healthKitUUID dedup.
     */
    @PostMapping("/sync")
    public String sync(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";
        if (!user.isFitbitConnected()) {
            ra.addFlashAttribute("fitbitError", "Fitbit is not connected.");
            return "redirect:/client/workouts";
        }

        Member member = memberRepo.findByEmail(user.getEmail()).orElse(null);
        if (member == null) {
            ra.addFlashAttribute("fitbitError", "Member profile not found.");
            return "redirect:/client/workouts";
        }

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(user);
        List<WorkoutLog> activities = fitbitService.fetchActivities(dbUser, member, 20);

        int imported = 0;
        for (WorkoutLog w : activities) {
            if (w.getHealthKitUUID() != null &&
                    workoutLogRepo.existsByHealthKitUUID(w.getHealthKitUUID())) {
                continue;
            }
            workoutLogRepo.save(w);
            imported++;
        }

        user.setFitbitAccessToken(dbUser.getFitbitAccessToken());
        user.setFitbitTokenExpiry(dbUser.getFitbitTokenExpiry());
        session.setAttribute("currentUser", user);

        if (imported > 0) {
            ra.addFlashAttribute("fitbitSuccess",
                "Synced " + imported + " new activit" + (imported == 1 ? "y" : "ies") + " from Fitbit!");
        } else {
            ra.addFlashAttribute("fitbitInfo", "No new Fitbit activities to import — already up to date.");
        }
        log.info("[FITBIT] Synced {} new activities for {}", imported, user.getEmail());
        return "redirect:/client/workouts";
    }

    /** Removes the stored Fitbit tokens — disconnects the integration. */
    @PostMapping("/disconnect")
    public String disconnect(HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        User dbUser = userRepo.findByEmail(user.getEmail()).orElse(null);
        if (dbUser != null) {
            dbUser.setFitbitAccessToken(null);
            dbUser.setFitbitRefreshToken(null);
            dbUser.setFitbitTokenExpiry(null);
            dbUser.setFitbitUserId(null);
            userRepo.save(dbUser);
        }
        user.setFitbitAccessToken(null);
        user.setFitbitUserId(null);
        session.setAttribute("currentUser", user);

        ra.addFlashAttribute("fitbitInfo", "Fitbit disconnected.");
        return "redirect:/client/workouts";
    }
}
