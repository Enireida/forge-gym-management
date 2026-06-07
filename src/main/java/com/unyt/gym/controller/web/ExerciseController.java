package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Exercise;
import com.unyt.gym.entity.Role;
import com.unyt.gym.entity.User;
import com.unyt.gym.entity.WorkoutPlan;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.repository.ExerciseRepository;
import com.unyt.gym.repository.WorkoutPlanRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * Pages for browsing the exercise library and curated workout plans.
 * Available to admins, trainers and clients.
 */
@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepo;
    private final WorkoutPlanRepository planRepo;

    public ExerciseController(ExerciseRepository exerciseRepo,
                              WorkoutPlanRepository planRepo) {
        this.exerciseRepo = exerciseRepo;
        this.planRepo = planRepo;
    }

    private void requireLogin(HttpSession session) {
        if (session.getAttribute("currentUser") == null) {
            throw new GymException("Please login first", 401);
        }
    }

    /* ========= ADMIN ROUTES ========= */

    @GetMapping("/admin/exercises")
    public String adminExercises(HttpSession session, Model model) {
        requireLogin(session);
        User u = (User) session.getAttribute("currentUser");
        if (u.getRole() != Role.ADMIN && u.getRole() != Role.MANAGER) {
            throw new GymException("Admin only", 403);
        }
        model.addAttribute("user", u);
        model.addAttribute("exercises", exerciseRepo.findAll());
        model.addAttribute("plans", planRepo.findAll());
        return "admin/exercises";
    }

    @PostMapping("/admin/exercises")
    public String addExercise(HttpSession session,
                              @RequestParam String name,
                              @RequestParam String muscleGroup,
                              @RequestParam String difficulty,
                              @RequestParam(required = false) String equipment,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam(required = false) String setsReps,
                              @RequestParam(required = false) String instructions) {
        requireLogin(session);
        Exercise e = new Exercise(name, muscleGroup, difficulty, equipment, instructions);
        e.setImageUrl(imageUrl);
        e.setSetsReps(setsReps);
        exerciseRepo.save(e);
        return "redirect:/admin/exercises";
    }

    @PostMapping("/admin/exercises/{id}/delete")
    public String deleteExercise(HttpSession session, @PathVariable Long id) {
        requireLogin(session);
        exerciseRepo.deleteById(id);
        return "redirect:/admin/exercises";
    }

    /* ========= CLIENT / TRAINER ROUTES ========= */

    @GetMapping({"/client/exercises", "/trainer/exercises"})
    public String browse(HttpSession session, Model model,
                         @RequestParam(required = false) String muscle,
                         @RequestParam(required = false) String level) {
        requireLogin(session);
        User u = (User) session.getAttribute("currentUser");
        model.addAttribute("user", u);

        if (muscle != null && !muscle.isBlank()) {
            model.addAttribute("exercises", exerciseRepo.findByMuscleGroup(muscle));
        } else {
            model.addAttribute("exercises", exerciseRepo.findAll());
        }
        if (level != null && !level.isBlank()) {
            model.addAttribute("plans", planRepo.findByLevel(level));
        } else {
            model.addAttribute("plans", planRepo.findAll());
        }
        model.addAttribute("muscle", muscle);
        model.addAttribute("level", level);

        // Use a single template, but the layout depends on the role.
        return u.getRole() == Role.CLIENT ? "client/exercises" : "trainer/exercises";
    }

    @PostMapping("/admin/exercises/{id}/edit")
    public String editExercise(HttpSession session, @PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String muscleGroup,
                               @RequestParam String difficulty,
                               @RequestParam(required = false) String equipment,
                               @RequestParam(required = false) String imageUrl,
                               @RequestParam(required = false) String setsReps,
                               @RequestParam(required = false) String instructions) {
        requireLogin(session);
        User u = (User) session.getAttribute("currentUser");
        if (u.getRole() != Role.ADMIN && u.getRole() != Role.MANAGER) {
            throw new GymException("Admin only", 403);
        }
        Exercise e = exerciseRepo.findById(id)
                .orElseThrow(() -> new GymException("Exercise not found", 404));
        e.setName(name);
        e.setMuscleGroup(muscleGroup);
        e.setDifficulty(difficulty);
        e.setEquipment(equipment);
        e.setImageUrl(imageUrl);
        e.setSetsReps(setsReps);
        e.setInstructions(instructions);
        exerciseRepo.save(e);
        return "redirect:/admin/exercises";
    }

    @GetMapping({"/client/plans/{id}", "/trainer/plans/{id}"})
    public String planDetail(HttpSession session, @PathVariable Long id, Model model) {
        requireLogin(session);
        User u = (User) session.getAttribute("currentUser");
        WorkoutPlan plan = planRepo.findById(id)
                .orElseThrow(() -> new GymException("Plan not found", 404));
        model.addAttribute("user", u);
        model.addAttribute("plan", plan);
        return u.getRole() == Role.CLIENT ? "client/plan-detail" : "trainer/plan-detail";
    }
}
