package com.unyt.gym.service;

import com.unyt.gym.entity.User;
import com.unyt.gym.exception.GymException;
import com.unyt.gym.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication service — login verification and password management.
 *
 * <p><b>Password migration:</b> If the stored password does NOT start with
 * {@code $2a$} / {@code $2b$} (i.e. plain-text legacy from data.sql), we
 * accept it once, immediately re-save as BCrypt, and continue — fully
 * transparent to the user.</p>
 *
 * <p>All password-change logic that was previously duplicated across
 * AdminController, ClientController, and TrainerController now lives here.
 * Controllers call {@link #changePassword} and get a clean result without
 * knowing anything about hashing.</p>
 */
@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* ── Login ────────────────────────────────────────────────────────── */

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) return Optional.empty();

        Optional<User> opt = userRepository.findByEmail(email.trim().toLowerCase());
        if (opt.isEmpty()) return Optional.empty();

        User user = opt.get();
        boolean match = verifyPassword(user, password);
        return match ? Optional.of(user) : Optional.empty();
    }

    public User requireAuthenticated(String email, String password) {
        return authenticate(email, password)
                .orElseThrow(() -> new GymException("Invalid email or password", 401));
    }

    /* ── Password verification ────────────────────────────────────────── */

    /**
     * Verifies a raw password against the stored value.
     * Handles both BCrypt hashes and legacy plain-text (auto-migrates on match).
     */
    public boolean verifyPassword(User user, String rawPassword) {
        String stored = user.getPassword();
        if (stored == null) return false;

        if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            // BCrypt — secure comparison
            return passwordEncoder.matches(rawPassword, stored);
        }

        // Legacy plain-text — compare directly then upgrade to BCrypt
        if (stored.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /* ── Password change (replaces duplicated logic in all controllers) ── */

    /**
     * Validates the current password and updates it to the new one.
     *
     * <p>Validation rules are enforced here so every controller gets the
     * same behaviour:</p>
     * <ul>
     *   <li>New password must be ≥ 6 characters</li>
     *   <li>Current password must be correct</li>
     * </ul>
     *
     * @return the updated {@link User} (with the new BCrypt hash) so the
     *         caller can refresh its session copy
     * @throws GymException 400 if validation fails
     */
    public User changePassword(String email,
                               String currentPassword,
                               String newPassword,
                               String confirmPassword) {

        if (newPassword == null || newPassword.length() < 6) {
            throw new GymException("Password must be at least 6 characters", 400);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new GymException("New passwords do not match", 400);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GymException("User not found", 404));

        if (!verifyPassword(user, currentPassword)) {
            throw new GymException("Current password is incorrect", 400);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Forcefully sets a new password without requiring the current one.
     * For admin-initiated resets only.
     */
    public void forceSetPassword(String email, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new GymException("Password must be at least 6 characters", 400);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GymException("User not found", 404));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /* ── Encoder access (for callers that need to hash externally) ─────── */

    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }
}
