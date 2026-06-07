package com.unyt.gym.controller.web;

import com.unyt.gym.entity.*;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.repository.*;
import com.unyt.gym.service.AuthService;
import com.unyt.gym.service.NotificationService;
import com.unyt.gym.service.PTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pages for trainers – list of members, ability to record measurements,
 * manage PT subscriptions and log completed sessions.
 */
@Controller
@RequestMapping("/trainer")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final MemberRepository memberRepo;
    private final MeasurementRepository measurementRepo;
    private final PTSubscriptionRepository ptRepo;
    private final PTPackageRepository packageRepo;
    private final StaffRepository staffRepo;
    private final PTService ptService;
    private final UserRepository userRepo;
    private final TrainerSlotRepository trainerSlotRepo;
    private final ClassBookingRepository classBookingRepo;
    private final AuthService authService;
    private final NotificationService notificationService;

    public TrainerController(MemberRepository memberRepo,
                             MeasurementRepository measurementRepo,
                             PTSubscriptionRepository ptRepo,
                             PTPackageRepository packageRepo,
                             StaffRepository staffRepo,
                             PTService ptService,
                             UserRepository userRepo,
                             TrainerSlotRepository trainerSlotRepo,
                             ClassBookingRepository classBookingRepo,
                             NotificationService notificationService,
                             AuthService authService) {
        this.notificationService = notificationService;
        this.memberRepo        = memberRepo;
        this.measurementRepo   = measurementRepo;
        this.ptRepo            = ptRepo;
        this.packageRepo       = packageRepo;
        this.staffRepo         = staffRepo;
        this.ptService         = ptService;
        this.userRepo          = userRepo;
        this.trainerSlotRepo   = trainerSlotRepo;
        this.classBookingRepo  = classBookingRepo;
        this.authService       = authService;
    }

    private Staff requireTrainer(HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null || u.getRole() != Role.TRAINER) {
            throw new GymException("Unauthorized - please login as trainer", 401);
        }
        return staffRepo.findByEmail(u.getEmail())
                .orElseThrow(() -> new GymException("Trainer profile not found", 404));
    }

    /** Returns the set of member IDs assigned to this trainer via PT subscriptions. */
    private Set<Long> trainerMemberIds(Long trainerId) {
        return ptRepo.findByTrainerId(trainerId).stream()
                .map(s -> s.getMember().getId())
                .collect(Collectors.toSet());
    }

    // ─────────────────────────────────────────────
    //  SEND NOTIFICATION (to assigned members)
    // ─────────────────────────────────────────────

    @GetMapping("/notifications")
    public String notificationForm(HttpSession session, Model model) {
        Staff trainer = requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        // Trainers message only their own clients
        Set<Long> mine = trainerMemberIds(trainer.getId());
        model.addAttribute("members", memberRepo.findAll().stream()
                .filter(m -> mine.contains(m.getId()))
                .collect(Collectors.toList()));
        return "trainer/send-notification";
    }

    @PostMapping("/notifications/send")
    public String sendNotification(HttpSession session,
                                   @RequestParam String title,
                                   @RequestParam String message,
                                   @RequestParam(required = false) Long memberId,
                                   RedirectAttributes ra) {
        Staff trainer = requireTrainer(session);
        if (title == null || title.isBlank() || message == null || message.isBlank()) {
            ra.addFlashAttribute("error", "Title and message are required.");
            return "redirect:/trainer/notifications";
        }
        Set<Long> mine = trainerMemberIds(trainer.getId());
        List<Member> recipients = memberRepo.findAll().stream()
                .filter(m -> mine.contains(m.getId()))
                .filter(m -> memberId == null || m.getId().equals(memberId))
                .collect(Collectors.toList());
        if (recipients.isEmpty()) {
            ra.addFlashAttribute("error", "No matching client to notify.");
            return "redirect:/trainer/notifications";
        }
        for (Member m : recipients) notificationService.sendToMember(m, title.trim(), message.trim());
        ra.addFlashAttribute("success", "Notification sent to " + recipients.size() + " client(s).");
        return "redirect:/trainer/notifications";
    }

    // ─────────────────────────────────────────────
    //  HOME
    // ─────────────────────────────────────────────

    @GetMapping
    public String home(HttpSession session, Model model) {
        Staff trainer = requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("trainer", trainer);
        model.addAttribute("members", memberRepo.findAll());
        model.addAttribute("subscriptions", ptRepo.findByTrainerId(trainer.getId()));
        model.addAttribute("recentSessions", ptService.sessionsByTrainer(trainer.getId()));
        return "trainer/home";
    }

    // ─────────────────────────────────────────────
    //  MY MEMBERS  (filtered to trainer's clients)
    // ─────────────────────────────────────────────

    @GetMapping("/members")
    public String members(HttpSession session, Model model) {
        Staff trainer = requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));

        Set<Long> ids = trainerMemberIds(trainer.getId());
        List<Member> myMembers = ids.isEmpty()
                ? List.of()   // no PT clients yet — show empty state, not all members
                : ids.stream()
                     .map(memberRepo::findById)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .sorted(Comparator.comparing(Member::getName))
                     .collect(Collectors.toList());

        model.addAttribute("members", myMembers);
        model.addAttribute("hasFilter", !ids.isEmpty());
        return "trainer/members";
    }

    // ─────────────────────────────────────────────
    //  MEASUREMENTS  (filtered to trainer's clients)
    // ─────────────────────────────────────────────

    @GetMapping("/measurements")
    public String measurements(HttpSession session, Model model) {
        Staff trainer = requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));

        Set<Long> ids = trainerMemberIds(trainer.getId());

        // All members for the "add measurement" dropdown
        model.addAttribute("members", ids.isEmpty()
                ? memberRepo.findAll()
                : ids.stream()
                     .map(memberRepo::findById)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .sorted(Comparator.comparing(Member::getName))
                     .collect(Collectors.toList()));

        // Only show measurements for this trainer's members (or all if none assigned)
        model.addAttribute("measurements", ids.isEmpty()
                ? measurementRepo.findAll()
                : ids.stream()
                     .flatMap(id -> measurementRepo.findByMemberIdOrderByDateDesc(id).stream())
                     .sorted(Comparator.comparing(com.unyt.gym.entity.Measurement::getDate).reversed())
                     .collect(Collectors.toList()));

        return "trainer/measurements";
    }

    @PostMapping("/measurements")
    public String addMeasurement(HttpSession session,
                                 @RequestParam Long memberId,
                                 @RequestParam Double weightKg,
                                 @RequestParam Double heightCm,
                                 @RequestParam(required = false) Double bodyFatPct,
                                 @RequestParam(required = false) String notes) {
        requireTrainer(session);
        Member m = memberRepo.findById(memberId)
                .orElseThrow(() -> new GymException("Member not found", 404));
        Measurement meas = new Measurement(m, LocalDate.now(), weightKg, heightCm);
        meas.setBodyFatPct(bodyFatPct);
        meas.setNotes(notes);
        measurementRepo.save(meas);
        return "redirect:/trainer/measurements";
    }

    // ─────────────────────────────────────────────
    //  PT SUBSCRIPTIONS
    // ─────────────────────────────────────────────

    @GetMapping("/pt")
    public String pt(HttpSession session, Model model) {
        Staff trainer = requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("trainer", trainer);
        model.addAttribute("subscriptions", ptRepo.findAll());
        model.addAttribute("members", memberRepo.findAll());
        model.addAttribute("packages", packageRepo.findByActiveTrue());
        model.addAttribute("trainers", staffRepo.findByRole("Trainer"));
        return "trainer/pt";
    }

    @PostMapping("/pt/new")
    public String newSubscription(HttpSession session,
                                  @RequestParam Long memberId,
                                  @RequestParam Long packageId) {
        Staff trainer = requireTrainer(session);
        ptService.createSubscription(memberId, trainer.getId(), packageId);
        return "redirect:/trainer/pt";
    }

    @PostMapping("/pt/{id}/assign")
    public String assignToMe(HttpSession session, @PathVariable Long id) {
        Staff trainer = requireTrainer(session);
        ptService.assignTrainer(id, trainer.getId());
        return "redirect:/trainer/pt";
    }

    @PostMapping("/pt/{id}/record")
    public String recordSession(HttpSession session,
                                @PathVariable Long id,
                                @RequestParam(required = false) String type,
                                @RequestParam(required = false) String notes) {
        Staff trainer = requireTrainer(session);
        ptService.recordSession(id, trainer.getId(), type, notes);
        return "redirect:/trainer/pt";
    }

    @GetMapping("/pt/{id}")
    public String viewSubscription(HttpSession session, @PathVariable Long id, Model model) {
        requireTrainer(session);
        PTSubscription sub = ptRepo.findById(id)
                .orElseThrow(() -> new GymException("Subscription not found", 404));
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("subscription", sub);
        model.addAttribute("sessions", ptService.sessionsForSubscription(id));
        return "trainer/pt-detail";
    }

    // ─────────────────────────────────────────────
    //  AVAILABILITY CALENDAR
    // ─────────────────────────────────────────────

    @GetMapping("/availability")
    public String availability(HttpSession session, Model model) {
        Staff trainer = requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("trainer", trainer);

        LocalDate today = LocalDate.now();
        List<TrainerSlot> slots = trainerSlotRepo
                .findByTrainerIdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
                        trainer.getId(), today, today.plusDays(30));
        model.addAttribute("slots", slots);
        return "trainer/availability";
    }

    @PostMapping("/availability/add")
    public String addSlot(HttpSession session,
                          @RequestParam String slotDate,
                          @RequestParam String startTime,
                          @RequestParam String endTime,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes ra) {
        Staff trainer = requireTrainer(session);

        // Validation
        LocalDate date;
        LocalTime start, end;
        try {
            date  = LocalDate.parse(slotDate);
            start = LocalTime.parse(startTime);
            end   = LocalTime.parse(endTime);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid date or time format.");
            return "redirect:/trainer/availability";
        }
        if (date.isBefore(LocalDate.now())) {
            ra.addFlashAttribute("error", "Slot date cannot be in the past.");
            return "redirect:/trainer/availability";
        }
        if (!end.isAfter(start)) {
            ra.addFlashAttribute("error", "End time must be after start time.");
            return "redirect:/trainer/availability";
        }

        TrainerSlot slot = new TrainerSlot();
        slot.setTrainer(trainer);
        slot.setSlotDate(date);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setStatus("AVAILABLE");
        slot.setNotes(notes);
        trainerSlotRepo.save(slot);

        log.info("Trainer {} added slot on {}", trainer.getId(), slotDate);
        ra.addFlashAttribute("success", "Slot added for " + slotDate + ".");
        return "redirect:/trainer/availability";
    }

    @PostMapping("/availability/{id}/delete")
    public String deleteSlot(HttpSession session,
                             @PathVariable Long id,
                             RedirectAttributes ra) {
        Staff trainer = requireTrainer(session);
        trainerSlotRepo.findById(id).ifPresent(s -> {
            if (s.getTrainer().getId().equals(trainer.getId())) trainerSlotRepo.delete(s);
        });
        ra.addFlashAttribute("success", "Slot removed.");
        return "redirect:/trainer/availability";
    }

    @PostMapping("/availability/{id}/block")
    public String blockSlot(HttpSession session,
                            @PathVariable Long id,
                            RedirectAttributes ra) {
        Staff trainer = requireTrainer(session);
        trainerSlotRepo.findById(id).ifPresent(s -> {
            if (s.getTrainer().getId().equals(trainer.getId())) {
                s.setStatus("BLOCKED");
                trainerSlotRepo.save(s);
            }
        });
        ra.addFlashAttribute("success", "Slot blocked.");
        return "redirect:/trainer/availability";
    }

    // ─────────────────────────────────────────────
    //  AI COACH
    // ─────────────────────────────────────────────

    @GetMapping("/ai")
    public String aiCoach(HttpSession session, Model model) {
        requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        return "trainer/ai";
    }

    // ─────────────────────────────────────────────
    //  EDIT PROFILE
    // ─────────────────────────────────────────────

    @PostMapping("/profile")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes ra) {
        Staff trainer = requireTrainer(session);
        User u = (User) session.getAttribute("currentUser");

        // Update User entity
        User dbUser = userRepo.findByEmail(u.getEmail())
                .orElseThrow(() -> new GymException("User not found", 404));
        if (name != null && !name.isBlank()) {
            dbUser.setName(name.trim());
            userRepo.save(dbUser);
            u.setName(dbUser.getName());
        }

        // Update Staff record
        if (name != null && !name.isBlank()) trainer.setName(name.trim());
        trainer.setPhone(phone != null && !phone.isBlank() ? phone.trim() : trainer.getPhone());
        staffRepo.save(trainer);

        log.info("Trainer {} updated their profile", u.getEmail());
        ra.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        return "redirect:/trainer";
    }

    // ─────────────────────────────────────────────
    //  CHANGE PASSWORD
    // ─────────────────────────────────────────────

    @GetMapping("/change-password")
    public String changePasswordForm(HttpSession session, Model model) {
        requireTrainer(session);
        model.addAttribute("user", session.getAttribute("currentUser"));
        model.addAttribute("portalBack", "/trainer");
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
            u.setPassword(updated.getPassword());
            session.setAttribute("currentUser", u);
            log.info("Trainer {} changed their password", u.getEmail());
            ra.addFlashAttribute("success", "Password updated successfully.");
        } catch (GymException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trainer/change-password";
    }
}
