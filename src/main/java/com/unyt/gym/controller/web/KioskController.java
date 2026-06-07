package com.unyt.gym.controller.web;

import com.unyt.gym.entity.Role;
import com.unyt.gym.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Front-door kiosk page — designed to run on a tablet near the gym entrance.
 *
 * <p><b>Security:</b> The kiosk requires activation by an admin before it
 * can be used. An admin navigates to {@code /admin/kiosk/start} which sets a
 * {@code kioskSession} flag in the current browser session and then redirects
 * here. Subsequent visits (e.g. after a page refresh) are allowed as long as
 * that flag exists — so the tablet stays on the kiosk screen without needing
 * the admin present.</p>
 *
 * <p>Without the flag (e.g. someone types the URL on a different device),
 * the visitor is redirected to the login page.</p>
 */
@Controller
public class KioskController {

    @GetMapping("/kiosk")
    public String kiosk(HttpSession session, Model model) {
        User u = (User) session.getAttribute("currentUser");
        Boolean kioskActivated = (Boolean) session.getAttribute("kioskSession");

        boolean isAdmin = u != null &&
                (u.getRole() == Role.ADMIN || u.getRole() == Role.MANAGER);

        if (!isAdmin && !Boolean.TRUE.equals(kioskActivated)) {
            // Not an admin and kiosk hasn't been activated — block access
            return "redirect:/login";
        }

        // Pass user + admin flag so the template can show the sidebar/topbar for admins
        model.addAttribute("user", u);
        model.addAttribute("isAdmin", isAdmin);
        return "kiosk";
    }
}
