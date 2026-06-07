package com.unyt.gym.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * System-level account used to authenticate against the gym application.
 *
 * <p>Members, trainers and admins all log in through this entity. The
 * domain-specific data (membership plan, trainer specialty, ...) lives on
 * {@link Member} and {@link Staff}, both of which reference this user.</p>
 *
 * <p><b>Course requirement:</b> R7 JPA / ORM — first of the JPA-mapped
 * entity classes.</p>
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, length = 160)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String password;   // stored hashed in production, plain for the demo

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 6-digit OTP for password reset; null when not in use. */
    @Column(name = "reset_token", length = 10)
    private String resetToken;

    /** Expiry timestamp for the OTP (15 minutes from generation). */
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    /** Strava OAuth2 integration fields. */
    @Column(name = "strava_access_token", length = 512)
    private String stravaAccessToken;

    @Column(name = "strava_refresh_token", length = 512)
    private String stravaRefreshToken;

    /** Unix timestamp (seconds) when the access token expires. */
    @Column(name = "strava_token_expiry")
    private Long stravaTokenExpiry;

    @Column(name = "strava_athlete_id")
    private Long stravaAthleteId;

    /** Google Fit OAuth2 integration fields. */
    @Column(name = "googlefit_access_token", length = 1024)
    private String googleFitAccessToken;

    @Column(name = "googlefit_refresh_token", length = 512)
    private String googleFitRefreshToken;

    @Column(name = "googlefit_token_expiry")
    private Long googleFitTokenExpiry;

    @Column(name = "googlefit_email", length = 200)
    private String googleFitEmail;

    /** Fitbit OAuth2 integration fields. */
    @Column(name = "fitbit_access_token", length = 512)
    private String fitbitAccessToken;

    @Column(name = "fitbit_refresh_token", length = 512)
    private String fitbitRefreshToken;

    /** Unix timestamp (seconds) when the Fitbit access token expires. */
    @Column(name = "fitbit_token_expiry")
    private Long fitbitTokenExpiry;

    @Column(name = "fitbit_user_id", length = 64)
    private String fitbitUserId;

    public User() { }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    public String getStravaAccessToken() { return stravaAccessToken; }
    public void setStravaAccessToken(String t) { this.stravaAccessToken = t; }
    public String getStravaRefreshToken() { return stravaRefreshToken; }
    public void setStravaRefreshToken(String t) { this.stravaRefreshToken = t; }
    public Long getStravaTokenExpiry() { return stravaTokenExpiry; }
    public void setStravaTokenExpiry(Long t) { this.stravaTokenExpiry = t; }
    public Long getStravaAthleteId() { return stravaAthleteId; }
    public void setStravaAthleteId(Long id) { this.stravaAthleteId = id; }
    public boolean isStravaConnected() { return stravaAccessToken != null; }

    public String getFitbitAccessToken() { return fitbitAccessToken; }
    public void setFitbitAccessToken(String t) { this.fitbitAccessToken = t; }
    public String getFitbitRefreshToken() { return fitbitRefreshToken; }
    public void setFitbitRefreshToken(String t) { this.fitbitRefreshToken = t; }
    public Long getFitbitTokenExpiry() { return fitbitTokenExpiry; }
    public void setFitbitTokenExpiry(Long t) { this.fitbitTokenExpiry = t; }
    public String getFitbitUserId() { return fitbitUserId; }
    public void setFitbitUserId(String id) { this.fitbitUserId = id; }
    public boolean isFitbitConnected() { return fitbitAccessToken != null; }

    public String getGoogleFitAccessToken() { return googleFitAccessToken; }
    public void setGoogleFitAccessToken(String t) { this.googleFitAccessToken = t; }
    public String getGoogleFitRefreshToken() { return googleFitRefreshToken; }
    public void setGoogleFitRefreshToken(String t) { this.googleFitRefreshToken = t; }
    public Long getGoogleFitTokenExpiry() { return googleFitTokenExpiry; }
    public void setGoogleFitTokenExpiry(Long t) { this.googleFitTokenExpiry = t; }
    public String getGoogleFitEmail() { return googleFitEmail; }
    public void setGoogleFitEmail(String e) { this.googleFitEmail = e; }
    public boolean isGoogleFitConnected() { return googleFitAccessToken != null; }
}
