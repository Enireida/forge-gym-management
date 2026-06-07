package com.unyt.gym.controller.web;

import com.unyt.gym.entity.WorkoutLog.WorkoutType;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.repository.MemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Serves the mobile Watch-sync mini-app — no login required.
 * Accessed via /sync/{memberId} from iPhone/Apple Watch.
 */
@Controller
public class WatchSyncController {

    private final MemberRepository memberRepo;

    public WatchSyncController(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    @GetMapping("/sync/{memberId}")
    public String syncPage(@PathVariable Long memberId, Model model) {
        var member = memberRepo.findById(memberId)
                .orElseThrow(() -> new GymException("Member not found", 404));
        model.addAttribute("member", member);
        model.addAttribute("memberId", memberId);
        model.addAttribute("workoutTypes", WorkoutType.values());
        return "watch-sync";
    }
}
