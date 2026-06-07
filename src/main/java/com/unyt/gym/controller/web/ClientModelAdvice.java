package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Role;
import com.unyt.gym.entity.User;
import com.unyt.gym.repository.MemberRepository;
import com.unyt.gym.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds the unread-notification count to every page rendered by {@link ClientController},
 * so the notification bell in the top bar shows a live badge on all member pages.
 */
@ControllerAdvice(assignableTypes = ClientController.class)
public class ClientModelAdvice {

    private final MemberRepository memberRepo;
    private final NotificationRepository notifRepo;

    public ClientModelAdvice(MemberRepository memberRepo, NotificationRepository notifRepo) {
        this.memberRepo = memberRepo;
        this.notifRepo  = notifRepo;
    }

    @ModelAttribute("unreadNotifs")
    public Long unreadNotifs(HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null || u.getRole() != Role.CLIENT) return null;
        return memberRepo.findByEmail(u.getEmail())
                .map(m -> notifRepo.countByMemberIdAndReadFalse(m.getId()))
                .orElse(null);
    }
}
