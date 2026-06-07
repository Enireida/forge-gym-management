package com.unyt.gym.controller.web;

import com.unyt.gym.entity.*;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.jdbc.RawReportDao;
import com.unyt.gym.repository.*;
import com.unyt.gym.service.AuthService;
import com.unyt.gym.service.MemberService;
import com.unyt.gym.service.NotificationService;
import com.unyt.gym.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MemberService             memberService;
    private final ReportService             reportService;
    private final RawReportDao              rawReportDao;
    private final MembershipPlanRepository  planRepo;
    private final StaffRepository           staffRepo;
    private final EquipmentRepository       equipmentRepo;
    private final AnnouncementRepository    announcementRepo;
    private final PaymentRepository         paymentRepo;
    private final AttendanceRepository      attendanceRepo;
    private final PTPackageRepository       packageRepo;
    private final MeasurementRepository     measurementRepo;
    private final UserRepository            userRepo;
    private final GymClassRepository        gymClassRepo;
    private final ClassBookingRepository    classBookingRepo;
    private final ClassRatingRepository     classRatingRepo;
    private final ClassWaitlistRepository   classWaitlistRepo;
    private final AuthService               authService;
    private final NotificationService       notificationService;
    private final MemberRepository          memberRepo;
    private final com.unyt.gym.service.CheckInEventService checkInEventService;

    public AdminController(MemberService memberService,
                           ReportService reportService,
                           RawReportDao rawReportDao,
                           MembershipPlanRepository planRepo,
                           StaffRepository staffRepo,
                           EquipmentRepository equipmentRepo,
                           AnnouncementRepository announcementRepo,
                           PaymentRepository paymentRepo,
                           AttendanceRepository attendanceRepo,
                           PTPackageRepository packageRepo,
                           MeasurementRepository measurementRepo,
                           UserRepository userRepo,
                           GymClassRepository gymClassRepo,
                           ClassBookingRepository classBookingRepo,
                           ClassRatingRepository classRatingRepo,
                           ClassWaitlistRepository classWaitlistRepo,
                           NotificationService notificationService,
                           MemberRepository memberRepo,
                           com.unyt.gym.service.CheckInEventService checkInEventService,
                           AuthService authService) {
        this.memberRepo          = memberRepo;
        this.checkInEventService = checkInEventService;
        this.notificationService = notificationService;
        this.memberService   = memberService;
        this.reportService   = reportService;
        this.rawReportDao    = rawReportDao;
        this.planRepo        = planRepo;
        this.staffRepo       = staffRepo;
        this.equipmentRepo   = equipmentRepo;
        this.announcementRepo= announcementRepo;
        this.paymentRepo     = paymentRepo;
        this.attendanceRepo  = attendanceRepo;
        this.packageRepo     = packageRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo        = userRepo;
        this.gymClassRepo      = gymClassRepo;
        this.classBookingRepo  = classBookingRepo;
        this.classRatingRepo   = classRatingRepo;
        this.classWaitlistRepo = classWaitlistRepo;
        this.authService     = authService;
    }

    private void requireAdmin(HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null || (u.getRole() != Role.ADMIN && u.getRole() != Role.MANAGER)) {
            throw new GymException("Unauthorized - please login as admin", 401);
        }
    }

    /* ===== DASHBOARD ===== */

    @GetMapping
    public String home(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("totalMembers", memberService.findAll().size());
        model.addAttribute("activeMembers", rawReportDao.activeMembers());
        model.addAttribute("checkinsToday", rawReportDao.checkinsToday());
        model.addAttribute("totalRevenue", rawReportDao.totalRevenue());
        model.addAttribute("statusBreakdown", reportService.memberStatusBreakdown());
        model.addAttribute("topSpenders", reportService.topSpenders(5));
        model.addAttribute("revenue30", reportService.revenueLastDays(30));
        return "admin/home";
    }

    /* ===== SEND NOTIFICATION (manual messages to members) ===== */

    @GetMapping("/notifications")
    public String sendNotificationForm(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("members", memberService.findAll());

        // ── Activity feed (Today + Yesterday) ──
        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime todayStart     = today.atStartOfDay();
        LocalDateTime tomorrowStart  = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();

        model.addAttribute("today", today);
        model.addAttribute("yesterday", yesterday);

        // Today
        model.addAttribute("newToday",
                memberRepo.findByCreatedAtBetweenOrderByCreatedAtDesc(todayStart, tomorrowStart));
        model.addAttribute("expiringToday",
                memberRepo.findByExpiryDate(today));
        model.addAttribute("expiringSoon",
                memberRepo.findByExpiryDateBetweenOrderByExpiryDateAsc(today.plusDays(1), today.plusDays(7)));

        // Yesterday
        model.addAttribute("newYesterday",
                memberRepo.findByCreatedAtBetweenOrderByCreatedAtDesc(yesterdayStart, todayStart));
        model.addAttribute("expiredYesterday",
                memberRepo.findByExpiryDate(yesterday));

        return "admin/send-notification";
    }

    /** Polled by the admin layout every few seconds to surface a live check-in toast. */
    @GetMapping(value = "/api/checkin-events", produces = "application/json")
    @ResponseBody
    public List<com.unyt.gym.service.CheckInEventService.CheckInEvent> apiCheckInEvents(
            HttpSession session,
            @RequestParam(defaultValue = "0") long since) {
        requireAdmin(session);
        return checkInEventService.eventsSince(since);
    }

    @PostMapping("/notifications/send")
    public String sendNotification(HttpSession session,
                                   @RequestParam String title,
                                   @RequestParam String message,
                                   @RequestParam(required = false) Long memberId,
                                   RedirectAttributes ra) {
        requireAdmin(session);
        if (title == null || title.isBlank() || message == null || message.isBlank()) {
            ra.addFlashAttribute("error", "Title and message are required.");
            return "redirect:/admin/notifications";
        }
        if (memberId != null) {
            Member m = memberService.findAll().stream()
                    .filter(x -> x.getId().equals(memberId))
                    .findFirst()
                    .orElseThrow(() -> new GymException("Member not found", 404));
            notificationService.sendToMember(m, title.trim(), message.trim());
            ra.addFlashAttribute("success", "Notification sent to " + m.getName() + ".");
        } else {
            int n = notificationService.sendToAllMembers(title.trim(), message.trim());
            ra.addFlashAttribute("success", "Notification broadcast to " + n + " member(s).");
        }
        return "redirect:/admin/notifications";
    }

    /* ===== MEMBERS ===== */

    @GetMapping("/members")
    public String members(HttpSession session, Model model,
                          @RequestParam(required = false) String q) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        List<Member> memberList = memberService.searchActive(q);
        model.addAttribute("members", memberList);
        model.addAttribute("query", q);
        model.addAttribute("plans", planRepo.findByActiveTrue());

        List<Member> allMembers = memberService.findAll();
        model.addAttribute("pendingMembers",
            allMembers.stream()
                .filter(m -> m.getStatus() == MemberStatus.PENDING)
                .collect(java.util.stream.Collectors.toList()));

        // Status counts for header chips
        LocalDate today = LocalDate.now();
        LocalDate in30 = today.plusDays(30);
        long activeCount   = allMembers.stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE
            && (m.getExpiryDate() == null || m.getExpiryDate().isAfter(in30))).count();
        long expiringCount = allMembers.stream().filter(m -> m.getStatus() == MemberStatus.ACTIVE
            && m.getExpiryDate() != null && !m.getExpiryDate().isAfter(in30)
            && !m.getExpiryDate().isBefore(today)).count();
        long expiredCount  = allMembers.stream().filter(m -> m.getStatus() == MemberStatus.EXPIRED).count();
        model.addAttribute("activeCount",   activeCount);
        model.addAttribute("expiringCount", expiringCount);
        model.addAttribute("expiredCount",  expiredCount);

        // Birthday perks
        int currentMonth = today.getMonthValue();
        Set<Long> birthdayMemberIds = new HashSet<>();
        for (Member m : memberList) {
            if (m.getBirthDate() != null && m.getBirthDate().getMonthValue() == currentMonth)
                birthdayMemberIds.add(m.getId());
        }
        model.addAttribute("birthdayMemberIds", birthdayMemberIds);
        return "admin/members";
    }

    @GetMapping("/members/new")
    public String addMemberPage(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("plans", planRepo.findByActiveTrue());
        return "admin/member-add";
    }

    @PostMapping("/members")
    public String addMember(HttpSession session,
                            @RequestParam String name,
                            @RequestParam(required = false) String surname,
                            @RequestParam String email,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) String rfidCode,
                            @RequestParam(required = false) String birthDate,
                            @RequestParam(required = false) String photoData,
                            @RequestParam Long planId,
                            RedirectAttributes ra) {
        requireAdmin(session);
        MembershipPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new GymException("Plan not found", 404));
        Member m = new Member();
        m.setName(name);
        m.setSurname(surname != null && !surname.isBlank() ? surname.trim() : null);
        m.setEmail(email);
        m.setPhone(phone);
        m.setRfidCode(rfidCode != null && !rfidCode.isBlank() ? rfidCode.trim() : null);
        if (birthDate != null && !birthDate.isBlank()) {
            m.setBirthDate(LocalDate.parse(birthDate));
        }
        if (photoData != null && !photoData.isBlank()) {
            m.setPhotoData(photoData);
        }
        m.setPlan(plan);
        m.setStartDate(LocalDate.now());
        m.setExpiryDate(LocalDate.now().plusMonths(plan.getDurationMonths()));

        // Create a login User account if one doesn't already exist
        String cleanEmail = email.trim().toLowerCase();
        String tempPassword = null;
        if (!userRepo.existsByEmail(cleanEmail)) {
            // Generate a temporary random password and pass it to the detail page
            // so the admin can hand it to the member in person
            tempPassword = UUID.randomUUID().toString().substring(0, 8);
            User newUser = new User(name.trim(), cleanEmail,
                    authService.encode(tempPassword), Role.CLIENT);
            User savedUser = userRepo.save(newUser);
            m.setEmail(cleanEmail);
            m.setUser(savedUser);
        } else {
            userRepo.findByEmail(cleanEmail).ifPresent(m::setUser);
        }

        Member saved = memberService.create(m);
        if (tempPassword != null) {
            ra.addFlashAttribute("tempPassword", tempPassword);
        }
        return "redirect:/admin/members/" + saved.getId();
    }

    /** Member detail / profile page */
    @GetMapping("/members/{id}")
    public String memberDetail(HttpSession session, @PathVariable Long id, Model model) {
        requireAdmin(session);
        Member m = memberService.findById(id);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("m", m);
        model.addAttribute("plans", planRepo.findByActiveTrue());
        model.addAttribute("payments", paymentRepo.findByMemberId(id));
        model.addAttribute("measurements", measurementRepo.findByMemberIdOrderByDateDesc(id));
        model.addAttribute("attendance", attendanceRepo.findByMemberIdOrderByDateDesc(id));
        return "admin/member-detail";
    }

    @PostMapping("/members/{id}/edit")
    public String editMember(HttpSession session, @PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) String phone,
                             @RequestParam Long planId,
                             @RequestParam String expiryDate,
                             @RequestParam String status) {
        requireAdmin(session);
        MembershipPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new GymException("Plan not found", 404));
        Member patch = new Member();
        patch.setName(name);
        patch.setPhone(phone != null && !phone.isBlank() ? phone : null);
        patch.setPlan(plan);
        patch.setExpiryDate(LocalDate.parse(expiryDate));
        patch.setStatus(MemberStatus.valueOf(status));
        memberService.update(id, patch);
        return "redirect:/admin/members";
    }

    /** Renew membership — extend expiry by N months */
    @PostMapping("/members/{id}/renew")
    public String renewMember(HttpSession session, @PathVariable Long id,
                              @RequestParam int months,
                              @RequestParam(required = false) Long planId) {
        requireAdmin(session);
        Member m = memberService.findById(id);
        // Optionally change plan
        if (planId != null) {
            MembershipPlan plan = planRepo.findById(planId)
                    .orElseThrow(() -> new GymException("Plan not found", 404));
            m.setPlan(plan);
        }
        // Extend from today or from current expiry (whichever is later)
        LocalDate base = (m.getExpiryDate() != null && m.getExpiryDate().isAfter(LocalDate.now()))
                ? m.getExpiryDate() : LocalDate.now();
        m.setExpiryDate(base.plusMonths(months));
        m.setStatus(MemberStatus.ACTIVE);
        memberService.update(id, m);
        // Record a payment for the renewal
        if (m.getPlan() != null) {
            BigDecimal amount = m.getPlan().getPrice().multiply(BigDecimal.valueOf(months));
            paymentRepo.save(new Payment(m, amount, "Cash",
                    "Membership renewal +" + months + " month(s)"));
        }
        return "redirect:/admin/members";
    }

    @PostMapping("/members/{id}/rfid")
    public String assignRfid(HttpSession session,
                             @PathVariable Long id,
                             @RequestParam String rfidCode) {
        requireAdmin(session);
        Member existing = memberService.findById(id);
        existing.setRfidCode(rfidCode == null || rfidCode.isBlank() ? null : rfidCode.trim());
        memberService.update(id, existing);
        return "redirect:/admin/members";
    }

    @PostMapping("/members/{id}/activate")
    public String activateMember(HttpSession session, @PathVariable Long id,
                                  RedirectAttributes ra) {
        requireAdmin(session);
        memberService.activate(id);
        ra.addFlashAttribute("success", "Anëtari u aktivizua me sukses!");
        return "redirect:/admin/members";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        memberService.delete(id);
        return "redirect:/admin/members";
    }

    /** Change password for a member (admin action) */
    @PostMapping("/members/{id}/password")
    public String changeMemberPassword(HttpSession session, @PathVariable Long id,
                                       @RequestParam String newPassword) {
        requireAdmin(session);
        if (newPassword == null || newPassword.length() < 6) {
            throw new GymException("Password must be at least 6 characters", 400);
        }
        Member m = memberService.findById(id);
        // forceSetPassword: admin-initiated, no current-password check needed
        authService.forceSetPassword(m.getEmail(), newPassword);
        return "redirect:/admin/members/" + id;
    }

    @PostMapping("/members/{id}/note")
    public String saveMemberNote(HttpSession session, @PathVariable Long id,
                                 @RequestParam(required = false) String adminNote,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        requireAdmin(session);
        Member m = memberService.findById(id);
        m.setAdminNote(adminNote != null && !adminNote.isBlank() ? adminNote.trim() : null);
        memberRepo.save(m);
        ra.addFlashAttribute("success", "Note saved.");
        return "redirect:/admin/members/" + id;
    }

    /* ===== PLANS ===== */

    @GetMapping("/plans")
    public String plans(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("plans", planRepo.findAll());
        return "admin/plans";
    }

    @GetMapping("/plans/new")
    public String newPlanPage(HttpSession session) {
        requireAdmin(session);
        return "admin/plan-add";
    }

    @PostMapping("/plans")
    public String addPlan(HttpSession session,
                          @RequestParam String name,
                          @RequestParam String description,
                          @RequestParam BigDecimal price,
                          @RequestParam int durationMonths) {
        requireAdmin(session);
        planRepo.save(new MembershipPlan(name, description, price, durationMonths));
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/{id}/edit")
    public String editPlan(HttpSession session, @PathVariable Long id,
                           @RequestParam String name,
                           @RequestParam(required = false) String description,
                           @RequestParam BigDecimal price,
                           @RequestParam int durationMonths) {
        requireAdmin(session);
        MembershipPlan p = planRepo.findById(id)
                .orElseThrow(() -> new GymException("Plan not found", 404));
        p.setName(name); p.setDescription(description);
        p.setPrice(price); p.setDurationMonths(durationMonths);
        planRepo.save(p);
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/{id}/toggle")
    public String togglePlan(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        MembershipPlan p = planRepo.findById(id)
                .orElseThrow(() -> new GymException("Plan not found", 404));
        p.setActive(!p.isActive());
        planRepo.save(p);
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/{id}/delete")
    public String deletePlan(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        planRepo.deleteById(id);
        return "redirect:/admin/plans";
    }

    /* ===== STAFF ===== */

    @GetMapping("/staff")
    public String staff(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("staff", staffRepo.findAll());
        return "admin/staff";
    }

    @GetMapping("/staff/new")
    public String newStaffPage(HttpSession session) {
        requireAdmin(session);
        return "admin/staff-add";
    }

    @PostMapping("/staff")
    public String addStaff(HttpSession session,
                           @RequestParam String name, @RequestParam String email,
                           @RequestParam String role,
                           @RequestParam(required = false) String specialty) {
        requireAdmin(session);
        staffRepo.save(new Staff(name, email, role, specialty));
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}/edit")
    public String editStaff(HttpSession session, @PathVariable Long id,
                            @RequestParam String name, @RequestParam String email,
                            @RequestParam(required = false) String phone,
                            @RequestParam String role,
                            @RequestParam(required = false) String specialty) {
        requireAdmin(session);
        Staff s = staffRepo.findById(id)
                .orElseThrow(() -> new GymException("Staff not found", 404));
        s.setName(name); s.setEmail(email); s.setPhone(phone);
        s.setRole(role); s.setSpecialty(specialty);
        staffRepo.save(s);
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}/delete")
    public String deleteStaff(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        staffRepo.deleteById(id);
        return "redirect:/admin/staff";
    }

    @PostMapping("/staff/{id}/toggle")
    public String toggleStaff(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        Staff s = staffRepo.findById(id)
                .orElseThrow(() -> new GymException("Staff not found", 404));
        s.setStatus("Active".equals(s.getStatus()) ? "Inactive" : "Active");
        staffRepo.save(s);
        return "redirect:/admin/staff";
    }

    /* ===== EQUIPMENT ===== */

    @GetMapping("/equipment")
    public String equipment(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("equipment", equipmentRepo.findAll());
        return "admin/equipment";
    }

    @GetMapping("/equipment/new")
    public String newEquipmentPage(HttpSession session) {
        requireAdmin(session);
        return "admin/equipment-add";
    }

    @PostMapping("/equipment")
    public String addEquipment(HttpSession session,
                               @RequestParam String name, @RequestParam String category,
                               @RequestParam int quantity) {
        requireAdmin(session);
        equipmentRepo.save(new Equipment(name, category, quantity));
        return "redirect:/admin/equipment";
    }

    @PostMapping("/equipment/{id}/edit")
    public String editEquipment(HttpSession session, @PathVariable Long id,
                                @RequestParam String name, @RequestParam String category,
                                @RequestParam int quantity,
                                @RequestParam(required = false) String notes) {
        requireAdmin(session);
        Equipment e = equipmentRepo.findById(id)
                .orElseThrow(() -> new GymException("Equipment not found", 404));
        e.setName(name); e.setCategory(category); e.setQuantity(quantity); e.setNotes(notes);
        equipmentRepo.save(e);
        return "redirect:/admin/equipment";
    }

    @PostMapping("/equipment/{id}/delete")
    public String deleteEquipment(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        equipmentRepo.deleteById(id);
        return "redirect:/admin/equipment";
    }

    @PostMapping("/equipment/{id}/toggle")
    public String toggleEquipment(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        Equipment e = equipmentRepo.findById(id)
                .orElseThrow(() -> new GymException("Equipment not found", 404));
        e.setStatus("Operational".equals(e.getStatus()) ? "Under Maintenance" : "Operational");
        equipmentRepo.save(e);
        return "redirect:/admin/equipment";
    }

    @PostMapping("/equipment/{id}/maintenance")
    public String logMaintenance(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        Equipment e = equipmentRepo.findById(id)
                .orElseThrow(() -> new GymException("Equipment not found", 404));
        e.setLastMaintenance(LocalDate.now());
        e.setStatus("Operational");
        equipmentRepo.save(e);
        return "redirect:/admin/equipment";
    }

    /* ===== ANNOUNCEMENTS ===== */

    @GetMapping("/announcements")
    public String announcements(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("announcements", announcementRepo.findAllByOrderByCreatedAtDesc());
        return "admin/announcements";
    }

    @GetMapping("/announcements/new")
    public String newAnnouncementPage(HttpSession session) {
        requireAdmin(session);
        return "admin/announcement-add";
    }

    @PostMapping("/announcements")
    public String addAnnouncement(HttpSession session,
                                  @RequestParam String title, @RequestParam String message,
                                  @RequestParam(defaultValue = "all") String audience) {
        requireAdmin(session);
        announcementRepo.save(new Announcement(title, message, audience));
        return "redirect:/admin/announcements";
    }

    @PostMapping("/announcements/{id}/edit")
    public String editAnnouncement(HttpSession session, @PathVariable Long id,
                                   @RequestParam String title, @RequestParam String message,
                                   @RequestParam String audience) {
        requireAdmin(session);
        Announcement a = announcementRepo.findById(id)
                .orElseThrow(() -> new GymException("Announcement not found", 404));
        a.setTitle(title); a.setMessage(message); a.setAudience(audience);
        announcementRepo.save(a);
        return "redirect:/admin/announcements";
    }

    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        announcementRepo.deleteById(id);
        return "redirect:/admin/announcements";
    }

    /* ===== PAYMENTS ===== */

    @GetMapping("/payments")
    public String payments(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("payments", paymentRepo.findAll());
        model.addAttribute("revenue30", reportService.revenueLastDays(30));
        model.addAttribute("members", memberService.findAll());
        return "admin/payments";
    }

    @PostMapping("/payments/manual")
    public String recordPayment(HttpSession session,
                                @RequestParam Long memberId,
                                @RequestParam BigDecimal amount,
                                @RequestParam String method,
                                @RequestParam(required = false) String description) {
        requireAdmin(session);
        Member m = memberService.findById(memberId);
        paymentRepo.save(new Payment(m, amount, method, description));
        return "redirect:/admin/payments";
    }

    @PostMapping("/payments/{id}/status")
    public String updatePaymentStatus(HttpSession session, @PathVariable Long id,
                                      @RequestParam String status) {
        requireAdmin(session);
        Payment p = paymentRepo.findById(id)
                .orElseThrow(() -> new GymException("Payment not found", 404));
        p.setStatus(status);
        paymentRepo.save(p);
        return "redirect:/admin/payments";
    }

    /* ===== ATTENDANCE ===== */

    @GetMapping("/attendance")
    public String attendance(HttpSession session, Model model,
                             @RequestParam(required = false) String date) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        LocalDate targetDate = (date != null && !date.isBlank())
                ? LocalDate.parse(date) : LocalDate.now();
        model.addAttribute("records", attendanceRepo.findByDate(targetDate));
        model.addAttribute("targetDate", targetDate);
        model.addAttribute("checkinsToday", rawReportDao.checkinsToday());
        return "admin/attendance";
    }

    /** Export attendance for a given date as CSV. */
    @GetMapping("/attendance/export")
    public void exportAttendance(HttpSession session,
                                 @RequestParam(required = false) String date,
                                 HttpServletResponse response) throws IOException {
        requireAdmin(session);
        LocalDate target = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
        var records = attendanceRepo.findByDate(target);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendance-" + target + ".csv");
        PrintWriter w = response.getWriter();
        w.println("Member,Plan,Date,Check-in,Check-out");
        for (var a : records) {
            String plan = (a.getMember().getPlan() != null) ? a.getMember().getPlan().getName() : "";
            w.println(csv(a.getMember().getName()) + "," + csv(plan) + "," +
                      target + "," + a.getCheckIn() + "," +
                      (a.getCheckOut() != null ? a.getCheckOut() : ""));
        }
    }
    private String csv(String s) { return "\"" + (s != null ? s.replace("\"","\"\"") : "") + "\""; }

    /* ===== PT PACKAGES ===== */

    @GetMapping("/pt-packages")
    public String ptPackages(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("packages", packageRepo.findAll());
        return "admin/pt-packages";
    }

    @GetMapping("/pt-packages/new")
    public String newPtPackagePage(HttpSession session) {
        requireAdmin(session);
        return "admin/pt-package-add";
    }

    @PostMapping("/pt-packages")
    public String addPackage(HttpSession session,
                             @RequestParam String name,
                             @RequestParam int sessions,
                             @RequestParam BigDecimal price,
                             @RequestParam(required = false) String description) {
        requireAdmin(session);
        PTPackage pkg = new PTPackage(name, sessions, price);
        pkg.setDescription(description);
        packageRepo.save(pkg);
        return "redirect:/admin/pt-packages";
    }

    @PostMapping("/pt-packages/{id}/edit")
    public String editPackage(HttpSession session, @PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam int sessions,
                              @RequestParam BigDecimal price,
                              @RequestParam(required = false) String description) {
        requireAdmin(session);
        PTPackage pkg = packageRepo.findById(id)
                .orElseThrow(() -> new GymException("Package not found", 404));
        pkg.setName(name); pkg.setSessions(sessions);
        pkg.setPrice(price); pkg.setDescription(description);
        packageRepo.save(pkg);
        return "redirect:/admin/pt-packages";
    }

    @PostMapping("/pt-packages/{id}/toggle")
    public String togglePackage(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        PTPackage pkg = packageRepo.findById(id)
                .orElseThrow(() -> new GymException("Package not found", 404));
        pkg.setActive(!pkg.isActive());
        packageRepo.save(pkg);
        return "redirect:/admin/pt-packages";
    }

    @PostMapping("/pt-packages/{id}/delete")
    public String deletePackage(HttpSession session, @PathVariable Long id) {
        requireAdmin(session);
        packageRepo.deleteById(id);
        return "redirect:/admin/pt-packages";
    }

    /* ===== GROUP CLASSES ===== */

    @GetMapping("/classes")
    public String classes(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        List<GymClass> classes = gymClassRepo.findAll();
        for (GymClass gc : classes) {
            gc.setEnrolledCount(classBookingRepo.countByGymClassIdAndStatus(gc.getId(), "CONFIRMED"));
        }
        model.addAttribute("classes", classes);
        model.addAttribute("staff", staffRepo.findAll());
        return "admin/classes";
    }

    @GetMapping("/classes/new")
    public String newClassPage(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("staff", staffRepo.findAll());
        return "admin/class-add";
    }

    @PostMapping("/classes")
    public String addClass(HttpSession session,
                           @RequestParam String name,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) Long trainerId,
                           @RequestParam String dateTime,
                           @RequestParam(defaultValue = "60") int durationMinutes,
                           @RequestParam(defaultValue = "20") int capacity,
                           @RequestParam(required = false) String category,
                           RedirectAttributes ra) {
        requireAdmin(session);

        // Validation
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("error", "Class name is required.");
            return "redirect:/admin/classes";
        }
        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(dateTime);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid date/time format.");
            return "redirect:/admin/classes";
        }
        if (dt.isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("error", "Class date must be in the future.");
            return "redirect:/admin/classes";
        }
        if (durationMinutes < 10 || durationMinutes > 300) {
            ra.addFlashAttribute("error", "Duration must be between 10 and 300 minutes.");
            return "redirect:/admin/classes";
        }
        if (capacity < 1 || capacity > 500) {
            ra.addFlashAttribute("error", "Capacity must be between 1 and 500.");
            return "redirect:/admin/classes";
        }

        GymClass gc = new GymClass();
        gc.setName(name.trim());
        gc.setDescription(description);
        gc.setDateTime(dt);
        gc.setDurationMinutes(durationMinutes);
        gc.setCapacity(capacity);
        gc.setCategory(category != null ? category.trim() : null);
        if (trainerId != null) {
            staffRepo.findById(trainerId).ifPresent(gc::setTrainer);
        }
        gymClassRepo.save(gc);
        ra.addFlashAttribute("success", "Class '" + gc.getName() + "' created!");
        return "redirect:/admin/classes";
    }

    @PostMapping("/classes/{id}/edit")
    public String editClass(HttpSession session, @PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) Long trainerId,
                            @RequestParam String dateTime,
                            @RequestParam(defaultValue = "60") int durationMinutes,
                            @RequestParam(defaultValue = "20") int capacity,
                            @RequestParam(required = false) String category,
                            RedirectAttributes ra) {
        requireAdmin(session);
        GymClass gc = gymClassRepo.findById(id)
                .orElseThrow(() -> new GymException("Class not found", 404));
        gc.setName(name.trim());
        gc.setDescription(description);
        gc.setCategory(category != null ? category.trim() : null);
        gc.setDurationMinutes(durationMinutes);
        gc.setCapacity(capacity);
        try {
            gc.setDateTime(java.time.LocalDateTime.parse(dateTime));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid date/time format.");
            return "redirect:/admin/classes";
        }
        if (trainerId != null) {
            staffRepo.findById(trainerId).ifPresent(gc::setTrainer);
        } else {
            gc.setTrainer(null);
        }
        gymClassRepo.save(gc);
        ra.addFlashAttribute("success", "Class '" + gc.getName() + "' updated.");
        return "redirect:/admin/classes";
    }

    @PostMapping("/classes/{id}/delete")
    public String deleteClass(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        requireAdmin(session);
        classWaitlistRepo.deleteAll(classWaitlistRepo.findByGymClassIdOrderByPositionAsc(id));
        classRatingRepo.deleteAll(classRatingRepo.findByGymClassIdOrderByCreatedAtDesc(id));
        classBookingRepo.deleteAll(classBookingRepo.findByGymClassIdOrderByBookedAtAsc(id));
        gymClassRepo.deleteById(id);
        ra.addFlashAttribute("success", "Class deleted.");
        return "redirect:/admin/classes";
    }

    @PostMapping("/classes/{id}/toggle")
    public String toggleClass(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        requireAdmin(session);
        GymClass gc = gymClassRepo.findById(id)
                .orElseThrow(() -> new GymException("Class not found", 404));
        gc.setActive(!gc.isActive());
        gymClassRepo.save(gc);
        ra.addFlashAttribute("success", "Class status updated.");
        return "redirect:/admin/classes";
    }

    /* ===== REPORTS ===== */

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("revenueByMonth", reportService.revenueByMonth());
        model.addAttribute("statusBreakdown", reportService.memberStatusBreakdown());
        model.addAttribute("topSpenders", reportService.topSpenders(10));
        model.addAttribute("recentAudit", rawReportDao.recentAudit(20));
        return "admin/reports";
    }

    /* ===== EDIT PROFILE (admin self) ===== */

    @PostMapping("/profile")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes ra) {
        requireAdmin(session);
        User u = (User) session.getAttribute("currentUser");

        // Update User entity
        User dbUser = userRepo.findByEmail(u.getEmail())
                .orElseThrow(() -> new GymException("User not found", 404));
        if (name != null && !name.isBlank()) {
            dbUser.setName(name.trim());
            userRepo.save(dbUser);
            // Update session copy so topbar reflects instantly
            u.setName(dbUser.getName());
        }

        // Also update Staff record if one is linked
        staffRepo.findByEmail(u.getEmail()).ifPresent(s -> {
            if (name != null && !name.isBlank()) s.setName(name.trim());
            s.setPhone(phone != null && !phone.isBlank() ? phone.trim() : s.getPhone());
            staffRepo.save(s);
        });

        ra.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        return "redirect:/admin";
    }

    /* ===== CHANGE PASSWORD (admin self) ===== */

    @GetMapping("/change-password")
    public String changePasswordForm(HttpSession session, Model model) {
        requireAdmin(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("portalBack", "/admin");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(HttpSession session,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        requireAdmin(session);
        User u = (User) session.getAttribute("currentUser");
        try {
            User updated = authService.changePassword(
                    u.getEmail(), currentPassword, newPassword, confirmPassword);
            // Refresh the session copy so it has the new hash
            u.setPassword(updated.getPassword());
            session.setAttribute("currentUser", u);
            ra.addFlashAttribute("success", "Password changed successfully.");
        } catch (GymException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/change-password";
    }

    /* ===== KIOSK ACTIVATION ===== */

    /**
     * Admin activates the door kiosk for the current browser session.
     * The kiosk page requires either an admin user OR a kioskSession flag.
     */
    @GetMapping("/kiosk/start")
    public String startKiosk(HttpSession session) {
        requireAdmin(session);
        session.setAttribute("kioskSession", Boolean.TRUE);
        return "redirect:/kiosk";
    }
}
