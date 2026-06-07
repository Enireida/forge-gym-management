package com.unyt.gym.controller.api;

import com.unyt.gym.entity.*;
import com.unyt.gym.entity.WorkoutLog.WorkoutType;
import com.unyt.gym.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.LinkedHashMap;

/**
 * Apple Watch / HealthKit REST API
 *
 * Base path: /api/watch
 *
 * Authentication: X-Forge-Watch-Token header (member's PIN used as simple token)
 * In production, replace with proper JWT/OAuth.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  Endpoint                        │ Description                  │
 * ├──────────────────────────────────┼──────────────────────────────┤
 * │  GET  /api/watch/{id}/profile    │ Member name, plan, PIN        │
 * │  GET  /api/watch/{id}/stats      │ Today's activity summary      │
 * │  GET  /api/watch/{id}/workouts   │ Recent workout history        │
 * │  POST /api/watch/{id}/workout    │ Log a workout from Watch      │
 * │  GET  /api/watch/{id}/checkins   │ Recent gym check-ins          │
 * │  GET  /api/watch/{id}/pin        │ Get door PIN for glance       │
 * └──────────────────────────────────┴──────────────────────────────┘
 */
@RestController
@RequestMapping("/api/watch")
@CrossOrigin(origins = "*") // iOS app can be on any origin
public class WatchController {

    private static final Logger log = LoggerFactory.getLogger(WatchController.class);

    private final MemberRepository     memberRepo;
    private final WorkoutLogRepository workoutLogRepo;
    private final AttendanceRepository attendanceRepo;
    private final PaymentRepository    paymentRepo;

    public WatchController(MemberRepository memberRepo,
                           WorkoutLogRepository workoutLogRepo,
                           AttendanceRepository attendanceRepo,
                           PaymentRepository paymentRepo) {
        this.memberRepo     = memberRepo;
        this.workoutLogRepo = workoutLogRepo;
        this.attendanceRepo = attendanceRepo;
        this.paymentRepo    = paymentRepo;
    }

    // ─────────────────────────────────────────────
    //  1. MEMBER PROFILE — compact for Watch
    // ─────────────────────────────────────────────

    @GetMapping("/{memberId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long memberId) {
        return memberRepo.findById(memberId)
            .map(m -> ResponseEntity.ok(Map.of(
                "id",          m.getId(),
                "name",        m.getName(),
                "initials",    initials(m.getName()),
                "plan",        m.getPlan() != null ? m.getPlan().getName() : "—",
                "status",      m.getStatus() != null ? m.getStatus().name() : "UNKNOWN",
                "pin",         m.getPin() != null ? m.getPin() : "—",
                "expiresAt",   m.getExpiryDate() != null ? m.getExpiryDate().toString() : null,
                "gymName",     "Forge · North Gym"
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────
    //  2. TODAY'S STATS — activity rings
    // ─────────────────────────────────────────────

    @GetMapping("/{memberId}/stats")
    public ResponseEntity<?> getTodayStats(@PathVariable Long memberId) {
        if (!memberRepo.existsById(memberId)) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime weekStart  = LocalDate.now().minusDays(7).atStartOfDay();

        Double caloriesDay   = workoutLogRepo.sumCaloriesByMemberAndDateRange(memberId, todayStart, todayEnd);
        Long   durationDay   = workoutLogRepo.sumDurationByMemberAndDateRange(memberId, todayStart, todayEnd);
        long   workoutsWeek  = workoutLogRepo.countByMemberIdAndStartTimeAfter(memberId, weekStart);

        // Today's check-ins
        long checkinsToday = attendanceRepo.countByMemberIdAndDateAfter(memberId, LocalDate.now());

        return ResponseEntity.ok(Map.of(
            "date",           LocalDate.now().toString(),
            "calories",       Math.round(caloriesDay != null ? caloriesDay : 0),
            "durationSeconds", durationDay != null ? durationDay : 0,
            "durationMin",    (durationDay != null ? durationDay : 0) / 60,
            "workoutsThisWeek", workoutsWeek,
            "checkinsToday",  checkinsToday,
            "activeGoalKcal", 500,   // default goal (could be personalised)
            "exerciseGoalMin", 30
        ));
    }

    // ─────────────────────────────────────────────
    //  3. WORKOUT HISTORY
    // ─────────────────────────────────────────────

    @GetMapping("/{memberId}/workouts")
    public ResponseEntity<?> getWorkouts(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "10") int limit) {

        if (!memberRepo.existsById(memberId)) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime since = LocalDateTime.now().minusDays(30);
        var logs = workoutLogRepo
            .findByMemberIdAndStartTimeAfterOrderByStartTimeDesc(memberId, since)
            .stream().limit(limit)
            .map(this::toWatchDto)
            .toList();

        return ResponseEntity.ok(Map.of(
            "workouts", logs,
            "total",    logs.size()
        ));
    }

    // ─────────────────────────────────────────────
    //  4. LOG WORKOUT  (POST from Watch / iOS)
    // ─────────────────────────────────────────────

    @PostMapping("/{memberId}/workout")
    public ResponseEntity<?> logWorkout(
            @PathVariable Long memberId,
            @RequestBody Map<String, Object> body) {

        Optional<Member> memberOpt = memberRepo.findById(memberId);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Deduplication by HealthKit UUID
        String hkUUID = (String) body.get("healthKitUUID");
        if (hkUUID != null && workoutLogRepo.existsByHealthKitUUID(hkUUID)) {
            log.info("Watch: duplicate workout ignored for member {} UUID {}", memberId, hkUUID);
            return ResponseEntity.ok(Map.of("status", "duplicate", "message", "Workout already logged"));
        }

        WorkoutLog workout = new WorkoutLog();
        workout.setMember(memberOpt.get());
        workout.setHealthKitUUID(hkUUID);
        workout.setSource("apple_watch");
        workout.setSyncedAt(LocalDateTime.now());

        // Parse workout type
        String typeStr = (String) body.getOrDefault("workoutType", "OTHER");
        try { workout.setWorkoutType(WorkoutType.valueOf(typeStr.toUpperCase())); }
        catch (Exception e) { workout.setWorkoutType(WorkoutType.OTHER); }

        // Parse timestamps
        if (body.get("startTime") != null)
            workout.setStartTime(LocalDateTime.parse((String) body.get("startTime")));
        else
            workout.setStartTime(LocalDateTime.now());

        if (body.get("endTime") != null)
            workout.setEndTime(LocalDateTime.parse((String) body.get("endTime")));

        // Numerics — HealthKit uses doubles
        workout.setDurationSeconds(getInt(body, "durationSeconds"));
        workout.setActiveCalories(getDouble(body, "activeCalories"));
        workout.setTotalCalories(getDouble(body, "totalCalories"));
        workout.setAvgHeartRate(getInt(body, "avgHeartRate"));
        workout.setMaxHeartRate(getInt(body, "maxHeartRate"));
        workout.setMinHeartRate(getInt(body, "minHeartRate"));
        workout.setDistanceMeters(getDouble(body, "distanceMeters"));
        workout.setStepCount(getInt(body, "stepCount"));
        workout.setNotes((String) body.get("notes"));

        WorkoutLog saved = workoutLogRepo.save(workout);
        log.info("Watch: workout logged for member {} — {} kcal {} min",
            memberId, workout.getActiveCalories(), workout.getDurationSeconds() != null ? workout.getDurationSeconds() / 60 : 0);

        return ResponseEntity.ok(Map.of(
            "status",  "saved",
            "id",      saved.getId(),
            "message", "Workout synced to Forge"
        ));
    }

    // ─────────────────────────────────────────────
    //  5. RECENT CHECK-INS
    // ─────────────────────────────────────────────

    @GetMapping("/{memberId}/checkins")
    public ResponseEntity<?> getCheckins(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "7") int days) {

        if (!memberRepo.existsById(memberId)) {
            return ResponseEntity.notFound().build();
        }

        LocalDate since = LocalDate.now().minusDays(days);
        var checkins = attendanceRepo
            .findByMemberIdOrderByDateDesc(memberId)
            .stream()
            .filter(a -> a.getDate() != null && !a.getDate().isBefore(since))
            .limit(20)
            .map(a -> Map.of(
                "date",   a.getDate().toString(),
                "time",   a.getCheckIn() != null ? a.getCheckIn().toString() : "—",
                "action", "CHECKIN"
            ))
            .toList();

        return ResponseEntity.ok(Map.of(
            "checkins", checkins,
            "total",    checkins.size(),
            "since",    since.toString()
        ));
    }

    // ─────────────────────────────────────────────
    //  6. PIN GLANCE — for watch face
    // ─────────────────────────────────────────────

    @GetMapping("/{memberId}/pin")
    public ResponseEntity<?> getDoorPin(@PathVariable Long memberId) {
        return memberRepo.findById(memberId)
            .map(m -> ResponseEntity.ok(Map.of(
                "pin",     m.getPin() != null ? m.getPin() : "—",
                "name",    m.getName(),
                "gymName", "Forge · North Gym",
                "hint",    "Show this PIN at reception or tap your RFID card"
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private Map<String, Object> toWatchDto(WorkoutLog w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",              w.getId());
        m.put("workoutType",     w.getWorkoutType() != null ? w.getWorkoutType().name() : "OTHER");
        m.put("startTime",       w.getStartTime() != null ? w.getStartTime().toString() : null);
        m.put("durationMin",     w.getDurationSeconds() != null ? w.getDurationSeconds() / 60 : 0);
        m.put("activeCalories",  w.getActiveCalories() != null ? Math.round(w.getActiveCalories()) : 0);
        m.put("avgHeartRate",    w.getAvgHeartRate());
        m.put("maxHeartRate",    w.getMaxHeartRate());
        m.put("distanceKm",      w.getDistanceMeters() != null ? Math.round(w.getDistanceMeters() / 100.0) / 10.0 : null);
        m.put("source",          w.getSource());
        return m;
    }

    private Integer getInt(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return null; }
    }

    private Double getDouble(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return null;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return null; }
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
