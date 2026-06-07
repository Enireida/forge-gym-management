package com.unyt.gym.controller.api;

import com.unyt.gym.entity.MemberStatus;
import com.unyt.gym.jdbc.RawReportDao;
import com.unyt.gym.service.ConcurrencyService;
import com.unyt.gym.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only statistics API used by the admin dashboard's charts and by
 * external monitoring tools.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsRestController {

    private final ReportService reportService;
    private final ConcurrencyService concurrencyService;
    private final RawReportDao rawReportDao;

    public StatsRestController(ReportService reportService,
                               ConcurrencyService concurrencyService,
                               RawReportDao rawReportDao) {
        this.reportService = reportService;
        this.concurrencyService = concurrencyService;
        this.rawReportDao = rawReportDao;
    }

    /** GET /api/stats/summary — quick KPI block. */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activeMembers", rawReportDao.activeMembers());
        result.put("checkinsToday", rawReportDao.checkinsToday());
        result.put("totalRevenue", rawReportDao.totalRevenue());
        result.put("revenue30d", reportService.revenueLastDays(30));
        result.put("statusBreakdown", reportService.memberStatusBreakdown());
        result.put("backgroundJobsCompleted", concurrencyService.getJobsCompleted());
        return ResponseEntity.ok(result);
    }

    /** GET /api/stats/revenue-by-month — JSON map keyed by YYYY-MM. */
    @GetMapping("/revenue-by-month")
    public ResponseEntity<Map<String, BigDecimal>> revenueByMonth() {
        Map<YearMonth, BigDecimal> raw = reportService.revenueByMonth();
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        raw.forEach((ym, val) -> out.put(ym.toString(), val));
        return ResponseEntity.ok(out);
    }

    /** GET /api/stats/forecast?days=30 — async forecast via CompletableFuture. */
    @GetMapping("/forecast")
    public ResponseEntity<Map<String, Object>> forecast(@RequestParam(defaultValue = "30") int days)
            throws Exception {
        BigDecimal projected = concurrencyService
                .forecastRenewalRevenueAsync(days)
                .get();    // demo: block until the async pipeline finishes
        return ResponseEntity.ok(Map.of(
                "horizonDays", days,
                "projectedRenewalRevenue", projected));
    }

    /** GET /api/stats/members-by-status — pie-chart friendly. */
    @GetMapping("/members-by-status")
    public ResponseEntity<Map<MemberStatus, Long>> membersByStatus() {
        return ResponseEntity.ok(reportService.memberStatusBreakdown());
    }

    /** GET /api/stats/revenue-range?from=YYYY-MM-DD&to=YYYY-MM-DD — revenue by day in range. */
    @GetMapping("/revenue-range")
    public ResponseEntity<Map<String, Object>> revenueRange(
            @RequestParam String from, @RequestParam String to) {
        LocalDate f = LocalDate.parse(from);
        LocalDate t = LocalDate.parse(to);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("byDay",  reportService.revenueByDay(f, t));
        result.put("total",  reportService.revenueByRange(f, t));
        result.put("topSpenders", reportService.topSpendersByRange(f, t, 10));
        return ResponseEntity.ok(result);
    }

    /** GET /api/stats/checkins-by-day?days=7 — attendance per day. */
    @GetMapping("/checkins-by-day")
    public ResponseEntity<Map<String, Integer>> checkinsByDay(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(rawReportDao.checkInsByDay(days));
    }

    /** GET /api/stats/new-members-by-month — new registrations per month. */
    @GetMapping("/new-members-by-month")
    public ResponseEntity<Map<String, Integer>> newMembersByMonth() {
        return ResponseEntity.ok(rawReportDao.newMembersByMonth());
    }

    /** GET /api/stats/members-by-plan — active members per plan. */
    @GetMapping("/members-by-plan")
    public ResponseEntity<Map<String, Integer>> membersByPlan() {
        return ResponseEntity.ok(rawReportDao.membersByPlan());
    }

    /** GET /api/stats/top-spenders?from=&to=&n=10 */
    @GetMapping("/top-spenders")
    public ResponseEntity<Map<String, BigDecimal>> topSpenders(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "10") int n) {
        LocalDate f = from != null ? LocalDate.parse(from) : LocalDate.now().minusYears(10);
        LocalDate t = to   != null ? LocalDate.parse(to)   : LocalDate.now();
        return ResponseEntity.ok(reportService.topSpendersByRange(f, t, n));
    }
}
