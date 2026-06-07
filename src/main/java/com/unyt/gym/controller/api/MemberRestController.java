package com.unyt.gym.controller.api;

import com.unyt.gym.entity.Attendance;
import com.unyt.gym.entity.Member;
import com.unyt.gym.generic.PaginatedResult;
import com.unyt.gym.service.AttendanceService;
import com.unyt.gym.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST endpoint for managing members and the door-kiosk PIN/RFID check-in.
 */
@RestController
@RequestMapping("/api/members")
public class MemberRestController {

    private final MemberService memberService;
    private final AttendanceService attendanceService;

    public MemberRestController(MemberService memberService,
                                AttendanceService attendanceService) {
        this.memberService = memberService;
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q) {

        List<Member> all = memberService.searchActive(q);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());

        PaginatedResult<Member> raw = new PaginatedResult<>(
                all.subList(from, to), all.size(), page, size);

        PaginatedResult<Map<String, Object>> dto = raw.map(m -> Map.of(
                "id", m.getId(),
                "name", m.getName(),
                "email", m.getEmail(),
                "plan", m.getPlan() == null ? "" : m.getPlan().getName(),
                "status", m.getStatus().name()));

        return ResponseEntity.ok(dto);
    }

    /** PIN check-in/out. */
    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkin(@RequestBody Map<String, String> body) {
        String pin = body.getOrDefault("pin", "");
        Attendance att = attendanceService.checkInOrOutByPin(pin);
        return buildCheckinResponse(att);
    }

    /** RFID check-in/out - invoked by the kiosk when a card is scanned. */
    @PostMapping("/checkin-rfid")
    public ResponseEntity<Map<String, Object>> checkinRfid(@RequestBody Map<String, String> body) {
        String code = body.getOrDefault("rfid", "").trim();
        Attendance att = attendanceService.checkInOrOutByRfid(code);
        return buildCheckinResponse(att);
    }

    /**
     * Live data feed for the door-kiosk page — returns counts and the latest events
     * so the kiosk can render the right-hand "Live feed" and "Currently inside" panels.
     */
    @GetMapping("/kiosk-feed")
    public ResponseEntity<Map<String, Object>> kioskFeed() {
        List<Attendance> today = attendanceService.getToday();

        // Sort newest first by check-in time
        List<Attendance> sorted = today.stream()
                .sorted(Comparator.comparing(Attendance::getCheckIn).reversed())
                .collect(Collectors.toList());

        long checkInsToday = sorted.size();
        List<Attendance> inside = sorted.stream()
                .filter(a -> a.getCheckOut() == null)
                .collect(Collectors.toList());

        // Live feed = newest 6 events (any action)
        List<Map<String, Object>> liveFeed = sorted.stream().limit(6).map(a -> {
            Member m = a.getMember();
            boolean isOut = a.getCheckOut() != null;
            String time = isOut ? a.getCheckOut().toString() : a.getCheckIn().toString();
            // Trim to HH:MM:SS if longer
            if (time.length() > 8) time = time.substring(0, 8);
            Map<String, Object> row = new HashMap<>();
            row.put("memberName", m.getName());
            row.put("memberPlan", m.getPlan() == null ? "" : m.getPlan().getName());
            row.put("action", isOut ? "checkout" : "checkin");
            row.put("time", time);
            row.put("initials", initials(m.getName()));
            return row;
        }).collect(Collectors.toList());

        // Currently inside = members whose latest event today has no checkOut
        List<Map<String, Object>> insideList = inside.stream().map(a -> {
            Member m = a.getMember();
            String t = a.getCheckIn().toString();
            if (t.length() > 8) t = t.substring(0, 8);
            Map<String, Object> row = new HashMap<>();
            row.put("memberName", m.getName());
            row.put("initials", initials(m.getName()));
            row.put("checkIn", t);
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> out = new HashMap<>();
        out.put("checkInsToday", checkInsToday);
        out.put("currentlyInside", inside.size());
        out.put("liveFeed", liveFeed);
        out.put("insideList", insideList);
        return ResponseEntity.ok(out);
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private ResponseEntity<Map<String, Object>> buildCheckinResponse(Attendance att) {
        Member m = att.getMember();
        Map<String, Object> res = new HashMap<>();
        res.put("memberId",    m.getId());
        res.put("memberName",  m.getName());
        res.put("memberPlan",  m.getPlan() == null ? "" : m.getPlan().getName());
        res.put("memberPhoto", m.getPhotoData() == null ? "" : m.getPhotoData());
        res.put("checkIn",     att.getCheckIn().toString());
        res.put("checkOut",    att.getCheckOut() == null ? "" : att.getCheckOut().toString());
        res.put("action",      att.getCheckOut() == null ? "checkin" : "checkout");
        return ResponseEntity.ok(res);
    }
}
