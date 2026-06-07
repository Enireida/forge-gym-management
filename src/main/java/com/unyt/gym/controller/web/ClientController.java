package com.unyt.gym.controller.web;

import com.unyt.gym.entity.*;
import com.unyt.gym.entity.WorkoutLog.WorkoutType;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.repository.*;
import com.unyt.gym.service.AuthService;
import com.unyt.gym.service.FoodCalorieHelper;
import com.unyt.gym.service.NotificationService;
import com.unyt.gym.util.CsvFieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pages for end-clients (gym members).
 */
@Controller
@RequestMapping("/client")
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final MemberRepository memberRepo;
    private final MeasurementRepository measurementRepo;
    private final PTSubscriptionRepository ptRepo;
    private final AnnouncementRepository announcementRepo;
    private final WorkoutLogRepository workoutLogRepo;
    private final PaymentRepository paymentRepo;
    private final UserRepository userRepo;
    private final FoodLogRepository foodLogRepo;
    private final GymClassRepository gymClassRepo;
    private final ClassBookingRepository classBookingRepo;
    private final TrainerSlotRepository trainerSlotRepo;
    private final FitnessGoalRepository goalRepo;
    private final WaterLogRepository waterLogRepo;
    private final NotificationRepository notifRepo;
    private final WorkoutTemplateRepository templateRepo;
    private final ClassWaitlistRepository waitlistRepo;
    private final ClassRatingRepository ratingRepo;
    private final StaffRepository staffRepo;
    private final AuthService authService;
    private final NotificationService notifService;

    public ClientController(MemberRepository memberRepo,
                            MeasurementRepository measurementRepo,
                            PTSubscriptionRepository ptRepo,
                            AnnouncementRepository announcementRepo,
                            WorkoutLogRepository workoutLogRepo,
                            PaymentRepository paymentRepo,
                            UserRepository userRepo,
                            FoodLogRepository foodLogRepo,
                            GymClassRepository gymClassRepo,
                            ClassBookingRepository classBookingRepo,
                            TrainerSlotRepository trainerSlotRepo,
                            FitnessGoalRepository goalRepo,
                            WaterLogRepository waterLogRepo,
                            NotificationRepository notifRepo,
                            WorkoutTemplateRepository templateRepo,
                            ClassWaitlistRepository waitlistRepo,
                            ClassRatingRepository ratingRepo,
                            StaffRepository staffRepo,
                            AuthService authService,
                            NotificationService notifService) {
        this.memberRepo       = memberRepo;
        this.measurementRepo  = measurementRepo;
        this.ptRepo           = ptRepo;
        this.announcementRepo = announcementRepo;
        this.workoutLogRepo   = workoutLogRepo;
        this.paymentRepo      = paymentRepo;
        this.userRepo         = userRepo;
        this.foodLogRepo      = foodLogRepo;
        this.gymClassRepo     = gymClassRepo;
        this.classBookingRepo = classBookingRepo;
        this.trainerSlotRepo  = trainerSlotRepo;
        this.goalRepo         = goalRepo;
        this.waterLogRepo     = waterLogRepo;
        this.notifRepo        = notifRepo;
        this.templateRepo     = templateRepo;
        this.waitlistRepo     = waitlistRepo;
        this.ratingRepo       = ratingRepo;
        this.staffRepo        = staffRepo;
        this.authService      = authService;
        this.notifService     = notifService;
    }

    private Member requireClient(HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null || u.getRole() != Role.CLIENT) {
            throw new GymException("Unauthorized — please login as a member", 401);
        }
        return memberRepo.findByEmail(u.getEmail())
                .orElseThrow(() -> new GymException("No member profile linked to this account", 404));
    }

    // ─────────────────────────────────────────────
    //  HOME
    // ─────────────────────────────────────────────

    @GetMapping
    public String home(HttpSession session, Model model) {
        Member me = requireClient(session);
        // Auto-generate a membership-expiry reminder if the plan ends within 7 days
        notifService.checkMembershipExpiry(me);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("announcements", announcementRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("unreadNotifs", notifRepo.countByMemberIdAndReadFalse(me.getId()));

        // ── Workout stats for dashboard cards ──
        LocalDateTime weekStart  = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        model.addAttribute("workoutsThisWeek",  workoutLogRepo.countByMemberIdAndStartTimeAfter(me.getId(), weekStart));
        model.addAttribute("workoutsThisMonth", workoutLogRepo.countByMemberIdAndStartTimeAfter(me.getId(), monthStart));
        model.addAttribute("calsBurnedWeek",    workoutLogRepo.sumCaloriesByMemberAndDateRange(me.getId(), weekStart, LocalDateTime.now()));
        model.addAttribute("calsBurnedMonth",   workoutLogRepo.sumCaloriesByMemberAndDateRange(me.getId(), monthStart, LocalDateTime.now()));

        // Workout streak
        List<LocalDate> days = workoutLogRepo.findDistinctWorkoutDays(me.getId(), LocalDate.now().minusDays(365).atStartOfDay());
        int streak = 0;
        LocalDate check = LocalDate.now();
        Set<LocalDate> workoutDays = new HashSet<>(days);
        while (workoutDays.contains(check) || (streak == 0 && workoutDays.contains(check.minusDays(1)))) {
            if (workoutDays.contains(check)) streak++;
            check = check.minusDays(1);
            if (streak > 365) break;
        }
        model.addAttribute("workoutStreak", streak);

        // Active goals count
        model.addAttribute("activeGoals", goalRepo.countByMemberIdAndStatus(me.getId(), FitnessGoal.GoalStatus.ACTIVE));

        // Today's water
        waterLogRepo.findByMemberIdAndDate(me.getId(), LocalDate.now())
                .ifPresentOrElse(
                    w -> model.addAttribute("todayWater", w.getGlasses()),
                    () -> model.addAttribute("todayWater", 0));

        // Today's calories
        Integer todayCals = foodLogRepo.sumCaloriesForDay(me.getId(), LocalDate.now());
        model.addAttribute("todayCalories", todayCals != null ? todayCals : 0);

        return "client/home";
    }

    // ─────────────────────────────────────────────
    //  MEMBERSHIP  (+ payment history)
    // ─────────────────────────────────────────────

    @GetMapping("/membership")
    public String membership(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("payments", paymentRepo.findByMemberId(me.getId()));
        return "client/membership";
    }

    @PostMapping("/membership/request-renewal")
    public String requestRenewal(HttpSession session,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        Member me = requireClient(session);
        notifService.notify(me,
            com.unyt.gym.entity.Notification.NotifType.ANNOUNCEMENT,
            "Renewal Request — " + me.getName(),
            me.getName() + " has requested a membership renewal. Plan: " +
                (me.getPlan() != null ? me.getPlan().getName() : "N/A") +
                " | Expires: " + me.getExpiryDate(),
            "/admin/members/" + me.getId());
        ra.addFlashAttribute("success", "Your renewal request has been sent to reception. We'll be in touch soon!");
        return "redirect:/client/membership";
    }

    // ─────────────────────────────────────────────
    //  PROGRESS
    // ─────────────────────────────────────────────

    @GetMapping("/progress")
    public String progress(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        List<Measurement> measurements = measurementRepo.findByMemberIdOrderByDateDesc(me.getId());
        model.addAttribute("measurements", measurements);

        // Chart data (chronological order for Chart.js)
        List<Measurement> chrono = new ArrayList<>(measurements);
        Collections.reverse(chrono);
        model.addAttribute("chartLabels", chrono.stream().map(m -> m.getDate().toString()).toList());
        model.addAttribute("chartWeight",  chrono.stream().map(m -> m.getWeightKg() != null ? m.getWeightKg() : 0).toList());
        model.addAttribute("chartBodyFat", chrono.stream().map(m -> m.getBodyFatPct() != null ? m.getBodyFatPct() : 0).toList());
        model.addAttribute("chartMuscle",  chrono.stream().map(m -> m.getMuscleMass() != null ? m.getMuscleMass() : 0).toList());

        // BMI calculation
        if (me.getWeightKg() != null && me.getHeightCm() != null && me.getHeightCm() > 0) {
            double h = me.getHeightCm() / 100.0;
            model.addAttribute("bmi", Math.round(me.getWeightKg() / (h * h) * 10.0) / 10.0);
        }
        return "client/progress";
    }

    // ─────────────────────────────────────────────
    //  PT SUBSCRIPTIONS
    // ─────────────────────────────────────────────

    @GetMapping("/pt")
    public String pt(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("subscriptions", ptRepo.findByMemberId(me.getId()));
        return "client/pt";
    }

    // ─────────────────────────────────────────────
    //  WORKOUTS  (history + manual log)
    // ─────────────────────────────────────────────

    @GetMapping("/workouts")
    public String workouts(HttpSession session, Model model) {
        Member me = requireClient(session);
        User u = (User) session.getAttribute("currentUser");
        model.addAttribute("user", u);
        model.addAttribute("me", me);
        model.addAttribute("workouts", workoutLogRepo.findByMemberIdOrderByStartTimeDesc(me.getId()));
        model.addAttribute("workoutTypes", WorkoutType.values());
        model.addAttribute("templates", templateRepo.findByMemberIdOrderByTimesUsedDesc(me.getId()));
        model.addAttribute("stravaConnected", u != null && u.isStravaConnected());
        model.addAttribute("stravaAthleteId", u != null ? u.getStravaAthleteId() : null);
        model.addAttribute("googleFitConnected", u != null && u.isGoogleFitConnected());
        model.addAttribute("googleFitEmail", u != null ? u.getGoogleFitEmail() : null);
        return "client/workouts";
    }

    @PostMapping("/workouts/log")
    public String logWorkout(HttpSession session,
                             @RequestParam String workoutType,
                             @RequestParam(required = false) Integer durationMinutes,
                             @RequestParam(required = false) Double calories,
                             @RequestParam(required = false) Integer avgHeartRate,
                             @RequestParam(required = false) String notes,
                             @RequestParam(required = false) String returnTo,
                             RedirectAttributes ra) {
        Member me = requireClient(session);

        WorkoutLog w = new WorkoutLog();
        w.setMember(me);
        w.setSource("manual");
        w.setStartTime(LocalDateTime.now());

        try { w.setWorkoutType(WorkoutType.valueOf(workoutType.toUpperCase())); }
        catch (Exception e) { w.setWorkoutType(WorkoutType.OTHER); }

        if (durationMinutes != null) {
            w.setDurationSeconds(durationMinutes * 60);
            w.setEndTime(w.getStartTime().plusMinutes(durationMinutes));
        }
        w.setActiveCalories(calories);
        w.setAvgHeartRate(avgHeartRate);
        w.setNotes(notes);

        workoutLogRepo.save(w);
        log.info("Manual workout logged for member {} — type={}", me.getId(), workoutType);
        ra.addFlashAttribute("success", "Workout logged successfully!");
        // Allow caller (e.g. Wearable page) to redirect back to itself
        String redirect = (returnTo != null && !returnTo.isBlank()
                && returnTo.startsWith("/client/")) ? returnTo : "/client/workouts";
        return "redirect:" + redirect;
    }

    /**
     * Import workouts from a CSV file exported from any fitness device.
     *
     * <p>Supported sources: Samsung Health, Fitbit, Garmin, Google Fit, Strava,
     * or any generic CSV with English column names.
     *
     * <p>Column recognition is flexible: headers are normalised (stripped of unit
     * labels like "(bpm)", "(kcal)", made lowercase, spaces → underscores) and
     * looked up in {@link CsvFieldMapper}'s alias table.  This means:
     * <ul>
     *   <li>{@code "Mean HR (bpm)"}, {@code "Avg. Heart Rate"}, {@code "avg_hr"},
     *       {@code "average_heart_rate"} all map to the same field.</li>
     *   <li>{@code "Duration(s)"} is read as seconds; {@code "Elapsed Time"} as
     *       seconds; {@code "Time"} (Garmin) as HH:MM:SS; bare {@code "Duration"}
     *       as HH:MM:SS or minutes depending on the cell value.</li>
     *   <li>{@code "Distance (m)"} is converted to km; {@code "Distance (mi)"}
     *       likewise.</li>
     * </ul>
     *
     * <p>Both comma ({@code ,}) and semicolon ({@code ;}) delimiters are supported.
     * A header row is required.  Missing columns are silently ignored.
     * Rows that cannot be parsed are counted as skipped.
     */
    @PostMapping("/workouts/import-csv")
    public String importCsv(HttpSession session,
                            @RequestParam("file") MultipartFile file,
                            RedirectAttributes ra) {
        Member me = requireClient(session);

        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a CSV file to import.");
            return "redirect:/client/workouts";
        }

        int imported = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // ── Read header ───────────────────────────────────────────
            String headerLine = reader.readLine();
            if (headerLine == null) {
                ra.addFlashAttribute("error", "The CSV file is empty.");
                return "redirect:/client/workouts";
            }
            // Strip BOM that some apps add at the start of the file
            if (headerLine.startsWith("﻿")) headerLine = headerLine.substring(1);

            // Auto-detect delimiter: use whichever gives more columns
            String delim = headerLine.split(";", -1).length
                         > headerLine.split(",", -1).length ? ";" : ",";

            String[] rawHeaders = headerLine.split(delim, -1);

            // ── Map columns with CsvFieldMapper ──────────────────────
            Map<String, Integer> col = CsvFieldMapper.mapHeaders(rawHeaders);
            String source = CsvFieldMapper.detectSource(rawHeaders);
            log.info("CSV import: source={}, recognised columns: {}", source, col.keySet());

            // Detect duration and distance units from the raw header text
            CsvFieldMapper.DurationUnit durationUnit = col.containsKey(CsvFieldMapper.DURATION)
                    ? CsvFieldMapper.detectDurationUnit(rawHeaders[col.get(CsvFieldMapper.DURATION)])
                    : CsvFieldMapper.DurationUnit.MINUTES;

            CsvFieldMapper.DistanceUnit distanceUnit = col.containsKey(CsvFieldMapper.DISTANCE)
                    ? CsvFieldMapper.detectDistanceUnit(rawHeaders[col.get(CsvFieldMapper.DISTANCE)], source)
                    : CsvFieldMapper.DistanceUnit.KM;

            log.info("CSV import: duration unit={}, distance unit={}", durationUnit, distanceUnit);

            // ── Parse rows ────────────────────────────────────────────
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cells = line.split(delim, -1);
                try {
                    WorkoutLog w = new WorkoutLog();
                    w.setMember(me);
                    w.setSource("csv_import");
                    w.setHealthKitUUID(UUID.randomUUID().toString());

                    // Date / start time
                    LocalDateTime start = parseDate(cellOf(cells, col.get(CsvFieldMapper.DATE)));
                    w.setStartTime(start != null ? start : LocalDateTime.now());

                    // Workout type
                    w.setWorkoutType(parseType(cellOf(cells, col.get(CsvFieldMapper.TYPE))));

                    // Duration — unit-aware (seconds / milliseconds / HH:MM:SS / minutes)
                    Integer durMin = CsvFieldMapper.parseDurationAsMinutes(
                            cellOf(cells, col.get(CsvFieldMapper.DURATION)), durationUnit);
                    if (durMin != null && durMin > 0) {
                        w.setDurationSeconds(durMin * 60);
                        w.setEndTime(w.getStartTime().plusMinutes(durMin));
                    }

                    // Calories
                    w.setActiveCalories(parseDouble(cellOf(cells, col.get(CsvFieldMapper.CALORIES))));

                    // Heart rates
                    w.setAvgHeartRate(parseInt(cellOf(cells, col.get(CsvFieldMapper.AVG_HR))));
                    w.setMaxHeartRate(parseInt(cellOf(cells, col.get(CsvFieldMapper.MAX_HR))));
                    w.setMinHeartRate(parseInt(cellOf(cells, col.get(CsvFieldMapper.MIN_HR))));

                    // Distance — unit-aware, stored internally as metres
                    Double distKm = CsvFieldMapper.parseDistanceAsKm(
                            cellOf(cells, col.get(CsvFieldMapper.DISTANCE)), distanceUnit);
                    if (distKm != null) w.setDistanceMeters(distKm * 1000.0);

                    // Steps
                    w.setStepCount(parseInt(cellOf(cells, col.get(CsvFieldMapper.STEPS))));

                    // Notes
                    String notes = cellOf(cells, col.get(CsvFieldMapper.NOTES));
                    if (notes != null && !notes.isBlank()) w.setNotes(notes);

                    workoutLogRepo.save(w);
                    imported++;
                } catch (Exception rowEx) {
                    log.warn("CSV import: skipped row — {}", rowEx.getMessage());
                    skipped++;
                }
            }
        } catch (Exception e) {
            log.error("CSV import failed", e);
            ra.addFlashAttribute("error", "CSV import failed: " + e.getMessage());
            return "redirect:/client/workouts";
        }

        String msg = "Imported " + imported + " workout" + (imported == 1 ? "" : "s") + ".";
        if (skipped > 0) msg += " Skipped " + skipped + " row" + (skipped == 1 ? "" : "s") + " due to errors.";
        ra.addFlashAttribute("success", msg);
        log.info("CSV import for member {} — imported={}, skipped={}", me.getId(), imported, skipped);
        return "redirect:/client/workouts";
    }

    // ── CSV helpers ──────────────────────────────────────────────────────────

    /** Returns the cell value at the given (nullable) column index, or null if out of range / blank. */
    private static String cellOf(String[] cells, Integer idx) {
        if (idx == null || idx < 0 || idx >= cells.length) return null;
        String v = cells[idx].trim().replace("\"", "");
        return v.isEmpty() ? null : v;
    }
    private static Integer parseInt(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.replaceAll("[^\\d-]", "")); } catch (Exception e) { return null; }
    }
    private static Double parseDouble(String s) {
        if (s == null) return null;
        try { return Double.parseDouble(s.replaceAll("[^\\d.\\-]", "")); } catch (Exception e) { return null; }
    }
    private static LocalDateTime parseDate(String s) {
        if (s == null) return null;
        s = s.trim().replace("\"", "");
        // Ordered most-specific to least-specific
        String[] dateTimePatterns = {
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
                "MMM d, yyyy 'at' h:mma",   // Strava: "May 20, 2026 at 7:30AM"
                "MMM d, yyyy 'at' hh:mma",  // Strava zero-padded
                "MM/dd/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm:ss"
        };
        String[] dateOnlyPatterns = {
                "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy",
                "dd-MM-yyyy", "yyyy/MM/dd",
                "MMM d, yyyy",              // "May 20, 2026"
                "d MMM yyyy"                // "20 May 2026"
        };
        for (String p : dateTimePatterns) {
            try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(p, java.util.Locale.ENGLISH)); }
            catch (Exception ignored) {}
        }
        for (String p : dateOnlyPatterns) {
            try { return LocalDate.parse(s, DateTimeFormatter.ofPattern(p, java.util.Locale.ENGLISH)).atStartOfDay(); }
            catch (Exception ignored) {}
        }
        return null;
    }
    private static WorkoutType parseType(String s) {
        if (s == null) return WorkoutType.OTHER;
        String up = s.trim().toUpperCase();
        try { return WorkoutType.valueOf(up); } catch (Exception ignored) {}
        // Common synonyms
        if (up.contains("RUN")) return WorkoutType.RUNNING;
        if (up.contains("BIKE") || up.contains("CYCL")) return WorkoutType.CYCLING;
        if (up.contains("SWIM")) return WorkoutType.SWIMMING;
        if (up.contains("YOGA")) return WorkoutType.YOGA;
        if (up.contains("HIIT") || up.contains("INTERVAL")) return WorkoutType.HIIT;
        if (up.contains("WEIGHT") || up.contains("LIFT") || up.contains("STRENGTH")) return WorkoutType.STRENGTH;
        if (up.contains("WALK") || up.contains("CARDIO")) return WorkoutType.CARDIO;
        return WorkoutType.OTHER;
    }

    // ─────────────────────────────────────────────
    //  WEARABLE DASHBOARD
    // ─────────────────────────────────────────────

    @GetMapping("/watch")
    public String watch(HttpSession session, Model model) {
        Member me = requireClient(session);
        User u = (User) session.getAttribute("currentUser");
        model.addAttribute("user", u);
        model.addAttribute("me", me);
        model.addAttribute("memberId", me.getId());
        model.addAttribute("workoutTypes", WorkoutType.values());
        model.addAttribute("recentWorkouts",
            workoutLogRepo.findByMemberIdAndStartTimeAfterOrderByStartTimeDesc(
                    me.getId(), LocalDateTime.now().minusDays(30))
                .stream().limit(8).toList());
        return "client/watch";
    }

    // ─────────────────────────────────────────────
    //  AI COACH
    // ─────────────────────────────────────────────

    @GetMapping("/ai")
    public String aiCoach(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        // Plan-tier gating: only premium plans unlock the AI Coach
        if (me.getPlan() == null || !me.getPlan().isAllowAiCoach()) {
            return "client/ai-locked";
        }
        return "client/ai";
    }

    // ─────────────────────────────────────────────
    //  EDIT PROFILE
    // ─────────────────────────────────────────────

    @PostMapping("/profile")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes ra) {
        Member me = requireClient(session);
        User u = (User) session.getAttribute("currentUser");

        // Update User entity
        User dbUser = userRepo.findByEmail(u.getEmail())
                .orElseThrow(() -> new GymException("User not found", 404));
        if (name != null && !name.isBlank()) {
            dbUser.setName(name.trim());
            userRepo.save(dbUser);
            u.setName(dbUser.getName());
        }
        // Update Member record (name + phone)
        if (name != null && !name.isBlank()) me.setName(name.trim());
        if (phone != null && !phone.isBlank()) me.setPhone(phone.trim());
        memberRepo.save(me);

        log.info("Client {} updated their profile", u.getEmail());
        ra.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        return "redirect:/client";
    }

    // ─────────────────────────────────────────────
    //  CHANGE PASSWORD
    // ─────────────────────────────────────────────

    @GetMapping("/change-password")
    public String changePasswordForm(HttpSession session, Model model) {
        requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("portalBack", "/client");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(HttpSession session,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null) return "redirect:/login";
        try {
            User updated = authService.changePassword(
                    u.getEmail(), currentPassword, newPassword, confirmPassword);
            u.setPassword(updated.getPassword());  // refresh session copy
            session.setAttribute("currentUser", u);
            ra.addFlashAttribute("success", "Password updated successfully.");
        } catch (GymException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/client/change-password";
    }

    // ─────────────────────────────────────────────
    //  FOOD LOG
    // ─────────────────────────────────────────────

    @GetMapping("/food")
    public String food(HttpSession session,
                       @RequestParam(required = false) String date,
                       Model model) {
        Member me = requireClient(session);
        LocalDate d = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        List<FoodLog> logs = foodLogRepo.findByMemberIdAndDateOrderByCreatedAtAsc(me.getId(), d);
        Integer totalCal = foodLogRepo.sumCaloriesForDay(me.getId(), d);

        double totalProtein = logs.stream().mapToDouble(fl -> fl.getProteinG() != null ? fl.getProteinG() : 0).sum();
        double totalCarbs   = logs.stream().mapToDouble(fl -> fl.getCarbsG()   != null ? fl.getCarbsG()   : 0).sum();
        double totalFat     = logs.stream().mapToDouble(fl -> fl.getFatG()     != null ? fl.getFatG()     : 0).sum();

        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("selectedDate", d);
        model.addAttribute("prevDate", d.minusDays(1));
        model.addAttribute("nextDate", d.plusDays(1));
        model.addAttribute("logs", logs);
        model.addAttribute("totalCalories", totalCal != null ? totalCal : 0);
        model.addAttribute("totalProtein",  Math.round(totalProtein * 10.0) / 10.0);
        model.addAttribute("totalCarbs",    Math.round(totalCarbs   * 10.0) / 10.0);
        model.addAttribute("totalFat",      Math.round(totalFat     * 10.0) / 10.0);

        // ── Water tracker (now lives inside the Food Log page) ──
        WaterLog water = waterLogRepo.findByMemberIdAndDate(me.getId(), d)
                .orElse(new WaterLog(me, d));
        int waterTargetMl = 2000;
        int waterPercent = waterTargetMl > 0
                ? Math.min(100, (int) Math.round(water.getMlTotal() * 100.0 / waterTargetMl))
                : 0;
        int caloriePercent = Math.min(100,
                (int) Math.round((totalCal != null ? totalCal : 0) * 100.0 / 2000.0));
        model.addAttribute("water", water);
        model.addAttribute("waterTargetMl", waterTargetMl);
        model.addAttribute("waterPercent", waterPercent);
        model.addAttribute("caloriePercent", caloriePercent);
        return "client/food";
    }

    @PostMapping("/food/log")
    public String logFood(HttpSession session,
                          @RequestParam String foodName,
                          @RequestParam(required = false) String mealType,
                          @RequestParam int quantityG,
                          @RequestParam(required = false) String date,
                          RedirectAttributes ra) {
        Member me = requireClient(session);
        LocalDate d = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();

        // Validation
        if (foodName == null || foodName.isBlank()) {
            ra.addFlashAttribute("error", "Please enter a food name.");
            return "redirect:/client/food" + (date != null ? "?date=" + date : "");
        }
        if (quantityG < 1 || quantityG > 5000) {
            ra.addFlashAttribute("error", "Quantity must be between 1 and 5000 grams.");
            return "redirect:/client/food" + (date != null ? "?date=" + date : "");
        }

        int[] macros = FoodCalorieHelper.lookup(foodName);
        double ratio = quantityG / 100.0;

        FoodLog fl = new FoodLog();
        fl.setMember(me);
        fl.setDate(d);
        fl.setMealType(mealType != null ? mealType : "Snack");
        fl.setFoodName(foodName.trim());
        fl.setQuantityG((double) quantityG);
        fl.setCalories((int) Math.round(macros[0] * ratio));
        fl.setProteinG(macros[1] * ratio);
        fl.setCarbsG(macros[2] * ratio);
        fl.setFatG(macros[3] * ratio);

        foodLogRepo.save(fl);
        log.info("Food logged for member {} — {} {}g", me.getId(), foodName, quantityG);
        ra.addFlashAttribute("success", "Food entry added!");
        return "redirect:/client/food?date=" + d;
    }

    @PostMapping("/food/delete/{id}")
    public String deleteFood(HttpSession session,
                             @PathVariable Long id,
                             @RequestParam(required = false) String date,
                             RedirectAttributes ra) {
        Member me = requireClient(session);
        foodLogRepo.findById(id).ifPresent(fl -> {
            if (fl.getMember().getId().equals(me.getId())) foodLogRepo.delete(fl);
        });
        ra.addFlashAttribute("success", "Entry removed.");
        String redirect = (date != null && !date.isBlank()) ? "?date=" + date : "";
        return "redirect:/client/food" + redirect;
    }

    @PostMapping("/food/edit/{id}")
    public String editFood(HttpSession session,
                           @PathVariable Long id,
                           @RequestParam String foodName,
                           @RequestParam(required = false) String mealType,
                           @RequestParam int quantityG,
                           @RequestParam(required = false) String date,
                           RedirectAttributes ra) {
        Member me = requireClient(session);
        String redirect = (date != null && !date.isBlank()) ? "?date=" + date : "";

        if (foodName == null || foodName.isBlank()) {
            ra.addFlashAttribute("error", "Please enter a food name.");
            return "redirect:/client/food" + redirect;
        }
        if (quantityG < 1 || quantityG > 5000) {
            ra.addFlashAttribute("error", "Quantity must be between 1 and 5000 grams.");
            return "redirect:/client/food" + redirect;
        }

        foodLogRepo.findById(id).ifPresent(fl -> {
            if (!fl.getMember().getId().equals(me.getId())) return;
            int[] macros = FoodCalorieHelper.lookup(foodName);
            double ratio = quantityG / 100.0;
            fl.setFoodName(foodName.trim());
            if (mealType != null && !mealType.isBlank()) fl.setMealType(mealType);
            fl.setQuantityG((double) quantityG);
            fl.setCalories((int) Math.round(macros[0] * ratio));
            fl.setProteinG(macros[1] * ratio);
            fl.setCarbsG(macros[2] * ratio);
            fl.setFatG(macros[3] * ratio);
            foodLogRepo.save(fl);
        });
        ra.addFlashAttribute("success", "Entry updated!");
        return "redirect:/client/food" + redirect;
    }

    // ─────────────────────────────────────────────
    //  GROUP CLASSES
    // ─────────────────────────────────────────────

    @GetMapping("/classes")
    public String classes(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);

        List<GymClass> upcoming =
                gymClassRepo.findByActiveTrueOrderByDateTimeAsc();

        // Which class IDs has this member already booked (CONFIRMED)?
        Set<Long> booked = classBookingRepo
                .findByMemberIdOrderByBookedAtDesc(me.getId()).stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()))
                .map(b -> b.getGymClass().getId())
                .collect(Collectors.toSet());

        // Attach enrolled counts
        upcoming.forEach(c ->
                c.setEnrolledCount(classBookingRepo.countByGymClassIdAndStatus(c.getId(), "CONFIRMED")));

        // Waitlist data & ratings
        Map<Long, Long> waitlistCounts = new HashMap<>();
        Map<Long, Boolean> onWaitlist = new HashMap<>();
        Map<Long, Double> avgRatings = new HashMap<>();
        Map<Long, Long> ratingCounts = new HashMap<>();
        Map<Long, Boolean> hasRated = new HashMap<>();
        for (GymClass c : upcoming) {
            waitlistCounts.put(c.getId(), waitlistRepo.countByGymClassId(c.getId()));
            onWaitlist.put(c.getId(), waitlistRepo.findByGymClassIdAndMemberId(c.getId(), me.getId()).isPresent());
            avgRatings.put(c.getId(), ratingRepo.averageRatingForClass(c.getId()));
            ratingCounts.put(c.getId(), ratingRepo.countByGymClassId(c.getId()));
            hasRated.put(c.getId(), ratingRepo.findByGymClassIdAndMemberId(c.getId(), me.getId()).isPresent());
        }

        // ── Calendar (next 7 days) ──
        List<LocalDate> weekDays = new ArrayList<>();
        Map<LocalDate, List<GymClass>> classesByDay = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now().plusDays(i);
            weekDays.add(d);
            classesByDay.put(d, new ArrayList<>());
        }
        for (GymClass c : upcoming) {
            if (c.getDateTime() == null) continue;
            LocalDate day = c.getDateTime().toLocalDate();
            if (classesByDay.containsKey(day)) classesByDay.get(day).add(c);
        }
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("classesByDay", classesByDay);
        model.addAttribute("today", LocalDate.now());

        model.addAttribute("classes", upcoming);
        model.addAttribute("bookedIds", booked);
        model.addAttribute("myBookings", classBookingRepo.findByMemberIdOrderByBookedAtDesc(me.getId()));
        model.addAttribute("waitlistCounts", waitlistCounts);
        model.addAttribute("onWaitlist", onWaitlist);
        model.addAttribute("avgRatings", avgRatings);
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("hasRated", hasRated);
        return "client/classes";
    }

    @PostMapping("/classes/{id}/book")
    public String bookClass(HttpSession session,
                            @PathVariable Long id,
                            RedirectAttributes ra) {
        Member me = requireClient(session);
        GymClass gc = gymClassRepo.findById(id)
                .orElseThrow(() -> new GymException("Class not found", 404));

        // Already booked?
        if (classBookingRepo.findByMemberIdAndGymClassId(me.getId(), id).isPresent()) {
            ra.addFlashAttribute("error", "You have already booked this class.");
            return "redirect:/client/classes";
        }
        // Class full?
        int enrolled = classBookingRepo.countByGymClassIdAndStatus(id, "CONFIRMED");
        if (gc.getCapacity() > 0 && enrolled >= gc.getCapacity()) {
            ra.addFlashAttribute("error", "Sorry, this class is full.");
            return "redirect:/client/classes";
        }

        ClassBooking booking = new ClassBooking();
        booking.setMember(me);
        booking.setGymClass(gc);
        booking.setStatus("CONFIRMED");
        classBookingRepo.save(booking);

        notifService.notify(me, Notification.NotifType.CLASS_REMINDER,
                "Class booked",
                "You're booked for " + gc.getName()
                        + (gc.getDateTime() != null ? " on " + gc.getDateTime()
                            .format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")) : "") + ".",
                "/client/classes");

        log.info("Member {} booked class {}", me.getId(), gc.getName());
        ra.addFlashAttribute("success", "Booked! See you at " + gc.getName() + ".");
        return "redirect:/client/classes";
    }

    @PostMapping("/classes/{id}/cancel")
    public String cancelClass(HttpSession session,
                              @PathVariable Long id,
                              RedirectAttributes ra) {
        Member me = requireClient(session);
        classBookingRepo.findByMemberIdAndGymClassId(me.getId(), id).ifPresent(b -> {
            b.setStatus("CANCELLED");
            classBookingRepo.save(b);
        });

        // A spot just opened — notify the first person on the waitlist who hasn't been told yet.
        gymClassRepo.findById(id).ifPresent(gc ->
            waitlistRepo.findByGymClassIdOrderByPositionAsc(id).stream()
                .filter(wl -> !wl.isNotified())
                .findFirst()
                .ifPresent(wl -> {
                    wl.setNotified(true);
                    waitlistRepo.save(wl);
                    notifService.notify(wl.getMember(), Notification.NotifType.WAITLIST_AVAILABLE,
                            "A spot opened up!",
                            "A place is now free in " + gc.getName()
                                    + ". Book it before someone else does!",
                            "/client/classes");
                }));

        ra.addFlashAttribute("success", "Booking cancelled.");
        return "redirect:/client/classes";
    }

    // ─────────────────────────────────────────────
    //  BOOK A TRAINER
    // ─────────────────────────────────────────────

    @GetMapping("/book-trainer")
    public String bookTrainer(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        List<TrainerSlot> availableSlots = trainerSlotRepo
                .findByStatusAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
                        "AVAILABLE", LocalDate.now());
        model.addAttribute("availableSlots", availableSlots);

        // Group available slots by date so the template can render a calendar view
        // (one card per day with all the time-slots inside).
        Map<LocalDate, List<TrainerSlot>> slotsByDay = new LinkedHashMap<>();
        for (TrainerSlot s : availableSlots) {
            slotsByDay.computeIfAbsent(s.getSlotDate(), k -> new ArrayList<>()).add(s);
        }
        model.addAttribute("slotsByDay", slotsByDay);

        model.addAttribute("mySlots",
                trainerSlotRepo.findByBookedByMemberIdOrderBySlotDateDesc(me.getId()));
        model.addAttribute("trainers", staffRepo.findByRoleAndStatus("Trainer", "Active"));
        return "client/book-trainer";
    }

    /** Public profile page for a single trainer, with inline PT slot booking. */
    @GetMapping("/trainer/{id}")
    public String trainerDetail(HttpSession session, @PathVariable Long id, Model model) {
        Member me = requireClient(session);
        Staff trainer = staffRepo.findById(id)
                .orElseThrow(() -> new GymException("Trainer not found", 404));
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("trainer", trainer);

        // Upcoming available slots for THIS trainer only (next 90 days)
        List<TrainerSlot> slots = trainerSlotRepo
                .findByTrainerIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        trainer.getId(), LocalDate.now(), LocalDate.now().plusDays(90))
                .stream()
                .filter(s -> "AVAILABLE".equals(s.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("availableSlots", slots);

        // Upcoming classes led by this trainer
        List<GymClass> upcoming = gymClassRepo.findAll().stream()
                .filter(c -> c.getTrainer() != null && c.getTrainer().getId().equals(trainer.getId()))
                .filter(c -> c.getDateTime() != null && c.getDateTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(GymClass::getDateTime))
                .collect(Collectors.toList());
        model.addAttribute("upcomingClasses", upcoming);

        // Rating aggregate
        model.addAttribute("avgRating", ratingRepo.averageRatingForTrainer(trainer.getId()));
        model.addAttribute("ratingCount", ratingRepo.countRatingsForTrainer(trainer.getId()));
        return "client/trainer-detail";
    }

    @PostMapping("/book-trainer/{slotId}/book")
    public String bookSlot(HttpSession session,
                           @PathVariable Long slotId,
                           RedirectAttributes ra) {
        Member me = requireClient(session);
        TrainerSlot slot = trainerSlotRepo.findById(slotId)
                .orElseThrow(() -> new GymException("Slot not found", 404));

        if (!"AVAILABLE".equals(slot.getStatus())) {
            ra.addFlashAttribute("error", "This slot is no longer available.");
            return "redirect:/client/book-trainer";
        }

        slot.setStatus("BOOKED");
        slot.setBookedByMember(me);
        trainerSlotRepo.save(slot);

        log.info("Member {} booked trainer slot {} on {}", me.getId(), slotId, slot.getSlotDate());
        ra.addFlashAttribute("success", "Session booked for " + slot.getSlotDate() + " at " + slot.getStartTime() + "!");
        return "redirect:/client/book-trainer";
    }

    @PostMapping("/book-trainer/{slotId}/cancel")
    public String cancelSlot(HttpSession session,
                             @PathVariable Long slotId,
                             RedirectAttributes ra) {
        Member me = requireClient(session);
        TrainerSlot slot = trainerSlotRepo.findById(slotId)
                .orElseThrow(() -> new GymException("Slot not found", 404));

        if (slot.getBookedByMember() != null && slot.getBookedByMember().getId().equals(me.getId())) {
            slot.setStatus("AVAILABLE");
            slot.setBookedByMember(null);
            trainerSlotRepo.save(slot);
            ra.addFlashAttribute("success", "Booking cancelled.");
        } else {
            ra.addFlashAttribute("error", "You cannot cancel this slot.");
        }
        return "redirect:/client/book-trainer";
    }

    // ─────────────────────────────────────────────
    //  WORKOUT EDIT & DELETE
    // ─────────────────────────────────────────────

    @PostMapping("/workouts/{id}/edit")
    public String editWorkout(HttpSession session,
                              @PathVariable Long id,
                              @RequestParam String workoutType,
                              @RequestParam(required = false) Integer durationMinutes,
                              @RequestParam(required = false) Double calories,
                              @RequestParam(required = false) Integer avgHeartRate,
                              @RequestParam(required = false) String notes,
                              RedirectAttributes ra) {
        Member me = requireClient(session);
        WorkoutLog w = workoutLogRepo.findById(id)
                .orElseThrow(() -> new GymException("Workout not found", 404));
        if (!w.getMember().getId().equals(me.getId()))
            throw new GymException("Unauthorized", 403);

        try { w.setWorkoutType(WorkoutType.valueOf(workoutType.toUpperCase())); }
        catch (Exception e) { w.setWorkoutType(WorkoutType.OTHER); }
        if (durationMinutes != null) {
            w.setDurationSeconds(durationMinutes * 60);
            w.setEndTime(w.getStartTime().plusMinutes(durationMinutes));
        }
        w.setActiveCalories(calories);
        w.setAvgHeartRate(avgHeartRate);
        w.setNotes(notes);
        workoutLogRepo.save(w);
        ra.addFlashAttribute("success", "Workout updated!");
        return "redirect:/client/workouts";
    }

    @PostMapping("/workouts/{id}/delete")
    public String deleteWorkout(HttpSession session,
                                @PathVariable Long id,
                                RedirectAttributes ra) {
        Member me = requireClient(session);
        workoutLogRepo.findById(id).ifPresent(w -> {
            if (w.getMember().getId().equals(me.getId())) workoutLogRepo.delete(w);
        });
        ra.addFlashAttribute("success", "Workout deleted.");
        return "redirect:/client/workouts";
    }

    // ─────────────────────────────────────────────
    //  EXPORT WORKOUT HISTORY (CSV)
    // ─────────────────────────────────────────────

    @GetMapping("/workouts/export")
    public void exportWorkouts(HttpSession session, HttpServletResponse response) throws Exception {
        Member me = requireClient(session);
        List<WorkoutLog> all = workoutLogRepo.findByMemberIdOrderByStartTimeDesc(me.getId());

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"forge-workouts.csv\"");

        PrintWriter w = response.getWriter();
        w.println("Date,Type,Duration (min),Calories,Avg HR,Max HR,Distance (km),Steps,Notes");
        for (WorkoutLog wl : all) {
            w.printf("%s,%s,%s,%s,%s,%s,%s,%s,\"%s\"%n",
                wl.getStartTime() != null ? wl.getStartTime().toLocalDate() : "",
                wl.getWorkoutType(),
                wl.getDurationSeconds() != null ? wl.getDurationSeconds() / 60 : "",
                wl.getActiveCalories() != null ? wl.getActiveCalories().intValue() : "",
                wl.getAvgHeartRate() != null ? wl.getAvgHeartRate() : "",
                wl.getMaxHeartRate() != null ? wl.getMaxHeartRate() : "",
                wl.getDistanceMeters() != null ? String.format("%.2f", wl.getDistanceMeters() / 1000.0) : "",
                wl.getStepCount() != null ? wl.getStepCount() : "",
                wl.getNotes() != null ? wl.getNotes().replace("\"", "'") : "");
        }
        w.flush();
    }

    // ─────────────────────────────────────────────
    //  WORKOUT TEMPLATES
    // ─────────────────────────────────────────────

    @PostMapping("/workouts/template/save")
    public String saveTemplate(HttpSession session,
                               @RequestParam String name,
                               @RequestParam String workoutType,
                               @RequestParam(required = false) Integer durationMinutes,
                               @RequestParam(required = false) Integer targetCalories,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes ra) {
        Member me = requireClient(session);
        WorkoutTemplate t = new WorkoutTemplate();
        t.setMember(me);
        t.setName(name.trim());
        try { t.setWorkoutType(WorkoutType.valueOf(workoutType.toUpperCase())); }
        catch (Exception e) { t.setWorkoutType(WorkoutType.OTHER); }
        t.setDurationMinutes(durationMinutes);
        t.setTargetCalories(targetCalories);
        t.setNotes(notes);
        templateRepo.save(t);
        ra.addFlashAttribute("success", "Template saved!");
        return "redirect:/client/workouts";
    }

    @PostMapping("/workouts/template/{id}/use")
    public String useTemplate(HttpSession session,
                              @PathVariable Long id,
                              RedirectAttributes ra) {
        Member me = requireClient(session);
        WorkoutTemplate t = templateRepo.findById(id)
                .orElseThrow(() -> new GymException("Template not found", 404));
        if (!t.getMember().getId().equals(me.getId()))
            throw new GymException("Unauthorized", 403);

        WorkoutLog w = new WorkoutLog();
        w.setMember(me);
        w.setSource("template");
        w.setStartTime(LocalDateTime.now());
        w.setWorkoutType(t.getWorkoutType());
        if (t.getDurationMinutes() != null) {
            w.setDurationSeconds(t.getDurationMinutes() * 60);
            w.setEndTime(w.getStartTime().plusMinutes(t.getDurationMinutes()));
        }
        if (t.getTargetCalories() != null)
            w.setActiveCalories(t.getTargetCalories().doubleValue());
        w.setNotes(t.getNotes());
        workoutLogRepo.save(w);

        t.setTimesUsed(t.getTimesUsed() + 1);
        templateRepo.save(t);

        ra.addFlashAttribute("success", "Workout logged from template \"" + t.getName() + "\"!");
        return "redirect:/client/workouts";
    }

    @PostMapping("/workouts/template/{id}/delete")
    public String deleteTemplate(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        Member me = requireClient(session);
        templateRepo.findById(id).ifPresent(t -> {
            if (t.getMember().getId().equals(me.getId())) templateRepo.delete(t);
        });
        ra.addFlashAttribute("success", "Template removed.");
        return "redirect:/client/workouts";
    }

    // ─────────────────────────────────────────────
    //  FITNESS GOALS
    // ─────────────────────────────────────────────

    @GetMapping("/goals")
    public String goals(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("activeGoals",  goalRepo.findByMemberIdAndStatusOrderByCreatedAtDesc(me.getId(), FitnessGoal.GoalStatus.ACTIVE));
        model.addAttribute("completedGoals", goalRepo.findByMemberIdAndStatusOrderByCreatedAtDesc(me.getId(), FitnessGoal.GoalStatus.COMPLETED));
        model.addAttribute("goalTypes", FitnessGoal.GoalType.values());
        return "client/goals";
    }

    @PostMapping("/goals/add")
    public String addGoal(HttpSession session,
                          @RequestParam String goalType,
                          @RequestParam String title,
                          @RequestParam Double targetValue,
                          @RequestParam(required = false) String unit,
                          @RequestParam(required = false) String targetDate,
                          RedirectAttributes ra) {
        Member me = requireClient(session);
        FitnessGoal g = new FitnessGoal();
        g.setMember(me);
        try { g.setGoalType(FitnessGoal.GoalType.valueOf(goalType)); }
        catch (Exception e) { g.setGoalType(FitnessGoal.GoalType.CUSTOM); }
        g.setTitle(title.trim());
        g.setTargetValue(targetValue);
        g.setUnit(unit);
        if (targetDate != null && !targetDate.isBlank()) g.setTargetDate(LocalDate.parse(targetDate));
        goalRepo.save(g);
        ra.addFlashAttribute("success", "Goal created!");
        return "redirect:/client/goals";
    }

    @PostMapping("/goals/{id}/update")
    public String updateGoalProgress(HttpSession session,
                                     @PathVariable Long id,
                                     @RequestParam Double currentValue,
                                     RedirectAttributes ra) {
        Member me = requireClient(session);
        FitnessGoal g = goalRepo.findById(id)
                .orElseThrow(() -> new GymException("Goal not found", 404));
        if (!g.getMember().getId().equals(me.getId()))
            throw new GymException("Unauthorized", 403);
        g.setCurrentValue(currentValue);
        if (currentValue >= g.getTargetValue()) {
            g.setStatus(FitnessGoal.GoalStatus.COMPLETED);
            notifRepo.save(new Notification(me, Notification.NotifType.GOAL_ACHIEVED,
                    "Goal achieved!", "You completed: " + g.getTitle(), "/client/goals"));
        }
        goalRepo.save(g);
        ra.addFlashAttribute("success", "Progress updated!");
        return "redirect:/client/goals";
    }

    @PostMapping("/goals/{id}/abandon")
    public String abandonGoal(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        Member me = requireClient(session);
        goalRepo.findById(id).ifPresent(g -> {
            if (g.getMember().getId().equals(me.getId())) {
                g.setStatus(FitnessGoal.GoalStatus.ABANDONED);
                goalRepo.save(g);
            }
        });
        ra.addFlashAttribute("success", "Goal archived.");
        return "redirect:/client/goals";
    }

    @PostMapping("/goals/{id}/edit")
    public String editGoal(HttpSession session,
                           @PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam Double targetValue,
                           @RequestParam(required = false) String unit,
                           @RequestParam(required = false) String targetDate,
                           RedirectAttributes ra) {
        Member me = requireClient(session);
        FitnessGoal g = goalRepo.findById(id)
                .orElseThrow(() -> new GymException("Goal not found", 404));
        if (!g.getMember().getId().equals(me.getId()))
            throw new GymException("Unauthorized", 403);
        if (title != null && !title.isBlank()) g.setTitle(title.trim());
        if (targetValue != null && targetValue > 0) g.setTargetValue(targetValue);
        g.setUnit(unit);
        g.setTargetDate((targetDate != null && !targetDate.isBlank()) ? LocalDate.parse(targetDate) : null);
        goalRepo.save(g);
        ra.addFlashAttribute("success", "Goal updated!");
        return "redirect:/client/goals";
    }

    @PostMapping("/goals/{id}/complete")
    public String completeGoal(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        Member me = requireClient(session);
        goalRepo.findById(id).ifPresent(g -> {
            if (!g.getMember().getId().equals(me.getId())) return;
            g.setStatus(FitnessGoal.GoalStatus.COMPLETED);
            if (g.getTargetValue() != null) g.setCurrentValue(g.getTargetValue());
            goalRepo.save(g);
            notifService.notify(me, Notification.NotifType.GOAL_ACHIEVED,
                    "Goal achieved!", "You completed: " + g.getTitle(), "/client/goals");
        });
        ra.addFlashAttribute("success", "Marked as complete. Well done!");
        return "redirect:/client/goals";
    }

    @PostMapping("/goals/{id}/delete")
    public String deleteGoal(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        Member me = requireClient(session);
        goalRepo.findById(id).ifPresent(g -> {
            if (g.getMember().getId().equals(me.getId())) goalRepo.delete(g);
        });
        ra.addFlashAttribute("success", "Goal deleted.");
        return "redirect:/client/goals";
    }

    // ─────────────────────────────────────────────
    //  WATER INTAKE TRACKER
    // ─────────────────────────────────────────────

    /** The standalone water page is gone — water now lives inside the Food Log. */
    @GetMapping("/water")
    public String water() {
        return "redirect:/client/food";
    }

    /** Helper: load (or create) the water log for a member on a given day. */
    private WaterLog waterFor(Member me, LocalDate d) {
        return waterLogRepo.findByMemberIdAndDate(me.getId(), d)
                .orElse(new WaterLog(me, d));
    }

    private LocalDate parseDateOrToday(String date) {
        return (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
    }

    /**
     * Add water. Accepts either a number of glasses (250ml each) or an explicit
     * millilitre amount (custom). Stored as a total ml; glasses are derived.
     */
    @PostMapping("/water/add")
    public String addWater(HttpSession session,
                           @RequestParam(required = false) Integer glasses,
                           @RequestParam(required = false) Integer ml,
                           @RequestParam(required = false) String date,
                           RedirectAttributes ra) {
        Member me = requireClient(session);
        LocalDate d = parseDateOrToday(date);
        int addMl = (ml != null && ml > 0) ? ml
                  : (glasses != null && glasses > 0 ? glasses : 1) * 250;
        addMl = Math.min(addMl, 5000); // sanity cap per action

        WaterLog w = waterFor(me, d);
        w.setMlTotal(Math.min(w.getMlTotal() + addMl, 20000));
        w.setGlasses((int) Math.round(w.getMlTotal() / 250.0));
        waterLogRepo.save(w);
        ra.addFlashAttribute("success", "+" + addMl + " ml of water logged.");
        return "redirect:/client/food?date=" + d;
    }

    /** Remove one glass (250ml), floored at zero. */
    @PostMapping("/water/remove")
    public String removeWater(HttpSession session,
                              @RequestParam(required = false) String date,
                              RedirectAttributes ra) {
        Member me = requireClient(session);
        LocalDate d = parseDateOrToday(date);
        WaterLog w = waterFor(me, d);
        w.setMlTotal(Math.max(0, w.getMlTotal() - 250));
        w.setGlasses((int) Math.round(w.getMlTotal() / 250.0));
        waterLogRepo.save(w);
        ra.addFlashAttribute("success", "Removed one glass.");
        return "redirect:/client/food?date=" + d;
    }

    @PostMapping("/water/reset")
    public String resetWater(HttpSession session,
                             @RequestParam(required = false) String date,
                             RedirectAttributes ra) {
        Member me = requireClient(session);
        LocalDate d = parseDateOrToday(date);
        waterLogRepo.findByMemberIdAndDate(me.getId(), d).ifPresent(w -> {
            w.setGlasses(0);
            w.setMlTotal(0);
            waterLogRepo.save(w);
        });
        ra.addFlashAttribute("success", "Water count reset.");
        return "redirect:/client/food?date=" + d;
    }

    // ─────────────────────────────────────────────
    //  NOTIFICATION CENTER
    // ─────────────────────────────────────────────

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        model.addAttribute("notifications", notifRepo.findByMemberIdOrderByCreatedAtDesc(me.getId()));
        model.addAttribute("unreadCount", notifRepo.countByMemberIdAndReadFalse(me.getId()));
        return "client/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(HttpSession session, RedirectAttributes ra) {
        Member me = requireClient(session);
        notifRepo.markAllReadForMember(me.getId());
        return "redirect:/client/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(HttpSession session, @PathVariable Long id) {
        Member me = requireClient(session);
        notifRepo.findById(id).ifPresent(n -> {
            if (n.getMember().getId().equals(me.getId())) {
                n.setRead(true);
                notifRepo.save(n);
            }
        });
        return "redirect:/client/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(HttpSession session, @PathVariable Long id) {
        Member me = requireClient(session);
        notifRepo.findById(id).ifPresent(n -> {
            if (n.getMember().getId().equals(me.getId())) notifRepo.delete(n);
        });
        return "redirect:/client/notifications";
    }

    @PostMapping("/notifications/clear-all")
    public String clearAllNotifications(HttpSession session, RedirectAttributes ra) {
        Member me = requireClient(session);
        notifRepo.deleteAllForMember(me.getId());
        ra.addFlashAttribute("success", "All notifications cleared.");
        return "redirect:/client/notifications";
    }

    @GetMapping("/notification-preferences")
    public String notificationPreferences(HttpSession session, Model model) {
        Member me = requireClient(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("me", me);
        // Build "enabled" map keyed by NotifType — opposite of disabledNotifTypes.
        Map<String, Boolean> enabled = new LinkedHashMap<>();
        for (Notification.NotifType t : Notification.NotifType.values()) {
            enabled.put(t.name(), !me.isNotifTypeDisabled(t.name()));
        }
        model.addAttribute("enabledMap", enabled);
        return "client/notification-preferences";
    }

    @PostMapping("/notification-preferences/save")
    public String saveNotificationPreferences(HttpSession session,
                                              @RequestParam(required = false) List<String> enabledTypes,
                                              RedirectAttributes ra) {
        Member me = requireClient(session);
        // Anything NOT in enabledTypes becomes disabled.
        List<String> disabled = new ArrayList<>();
        Set<String> on = enabledTypes != null ? new HashSet<>(enabledTypes) : Collections.emptySet();
        for (Notification.NotifType t : Notification.NotifType.values()) {
            if (!on.contains(t.name())) disabled.add(t.name());
        }
        me.setDisabledNotifTypes(disabled.isEmpty() ? null : String.join(",", disabled));
        memberRepo.save(me);
        ra.addFlashAttribute("success", "Notification preferences saved.");
        return "redirect:/client/notification-preferences";
    }

    // ─────────────────────────────────────────────
    //  CLASS WAITLIST
    // ─────────────────────────────────────────────

    @PostMapping("/classes/{id}/waitlist")
    public String joinWaitlist(HttpSession session,
                               @PathVariable Long id,
                               RedirectAttributes ra) {
        Member me = requireClient(session);
        GymClass gc = gymClassRepo.findById(id)
                .orElseThrow(() -> new GymException("Class not found", 404));

        if (waitlistRepo.findByGymClassIdAndMemberId(id, me.getId()).isPresent()) {
            ra.addFlashAttribute("error", "You are already on the waitlist.");
            return "redirect:/client/classes";
        }

        int position = (int) waitlistRepo.countByGymClassId(id) + 1;
        waitlistRepo.save(new ClassWaitlist(gc, me, position));
        ra.addFlashAttribute("success", "Added to waitlist (position #" + position + ").");
        return "redirect:/client/classes";
    }

    @PostMapping("/classes/{id}/waitlist/cancel")
    public String leaveWaitlist(HttpSession session,
                                @PathVariable Long id,
                                RedirectAttributes ra) {
        Member me = requireClient(session);
        waitlistRepo.findByGymClassIdAndMemberId(id, me.getId()).ifPresent(waitlistRepo::delete);
        ra.addFlashAttribute("success", "Removed from waitlist.");
        return "redirect:/client/classes";
    }

    // ─────────────────────────────────────────────
    //  CLASS RATING / FEEDBACK
    // ─────────────────────────────────────────────

    @PostMapping("/classes/{id}/rate")
    public String rateClass(HttpSession session,
                            @PathVariable Long id,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            RedirectAttributes ra) {
        Member me = requireClient(session);
        GymClass gc = gymClassRepo.findById(id)
                .orElseThrow(() -> new GymException("Class not found", 404));

        ClassRating r = ratingRepo.findByGymClassIdAndMemberId(id, me.getId())
                .orElse(new ClassRating(gc, me, rating));
        r.setRating(Math.max(1, Math.min(5, rating)));
        if (comment != null && !comment.isBlank()) r.setComment(comment.trim());
        ratingRepo.save(r);
        ra.addFlashAttribute("success", "Thanks for your feedback!");
        return "redirect:/client/classes";
    }

    // ─────────────────────────────────────────────
    //  RECEIPT DOWNLOAD
    // ─────────────────────────────────────────────

    @GetMapping("/membership/receipt/{paymentId}")
    public void downloadReceipt(HttpSession session,
                                @PathVariable Long paymentId,
                                HttpServletResponse response) throws Exception {
        Member me = requireClient(session);
        Payment p = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new GymException("Payment not found", 404));
        if (!p.getMember().getId().equals(me.getId()))
            throw new GymException("Unauthorized", 403);

        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"forge-receipt-" + paymentId + ".txt\"");

        PrintWriter w = response.getWriter();
        w.println("══════════════════════════════════════");
        w.println("           FORGE GYM - RECEIPT        ");
        w.println("══════════════════════════════════════");
        w.println();
        w.println("  Receipt #:    " + p.getId());
        w.println("  Date:         " + (p.getDate() != null ? p.getDate() : "N/A"));
        w.println("  Member:       " + me.getName());
        w.println("  Email:        " + me.getEmail());
        w.println();
        w.println("  Description:  " + (p.getDescription() != null ? p.getDescription() : "Membership Payment"));
        w.println("  Amount:       " + p.getAmount() + " ALL");
        w.println("  Method:       " + (p.getMethod() != null ? p.getMethod() : "N/A"));
        w.println("  Status:       " + (p.getStatus() != null ? p.getStatus() : "Completed"));
        w.println();
        w.println("──────────────────────────────────────");
        w.println("  Thank you for choosing Forge Gym!   ");
        w.println("══════════════════════════════════════");
        w.flush();
    }

    // ─────────────────────────────────────────────
    //  WORKOUT STATS (JSON for Chart.js)
    // ─────────────────────────────────────────────

    @GetMapping("/stats/weekly-calories")
    @ResponseBody
    public Map<String, Object> weeklyCalories(HttpSession session) {
        Member me = requireClient(session);
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            labels.add(d.getDayOfWeek().toString().substring(0, 3));
            Double cal = workoutLogRepo.sumCaloriesByMemberAndDateRange(
                    me.getId(), d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            data.add(cal != null ? cal : 0);
        }
        return Map.of("labels", labels, "data", data);
    }

    @GetMapping("/stats/weekly-duration")
    @ResponseBody
    public Map<String, Object> weeklyDuration(HttpSession session) {
        Member me = requireClient(session);
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            labels.add(d.getDayOfWeek().toString().substring(0, 3));
            Long secs = workoutLogRepo.sumDurationByMemberAndDateRange(
                    me.getId(), d.atStartOfDay(), d.plusDays(1).atStartOfDay());
            data.add(secs != null ? secs / 60 : 0);
        }
        return Map.of("labels", labels, "data", data);
    }
}
