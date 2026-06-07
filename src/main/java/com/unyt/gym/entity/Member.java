package com.unyt.gym.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A paying gym member.
 *
 * <p>Linked to a {@link MembershipPlan} (many members - one plan) and to a
 * {@link User} for login credentials. The PIN field is the 4-digit code
 * used as a backup login; the rfidCode is the 10-digit code on the door card.</p>
 *
 * <p><b>Course requirement:</b> R7 JPA / ORM.</p>
 */
@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_members_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_members_pin", columnNames = "pin"),
        @UniqueConstraint(name = "uk_members_rfid", columnNames = "rfid_code")
})
public class Member {

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

    @Column(length = 30)
    private String phone;

    /** 4-digit PIN used as a backup login. */
    @Column(nullable = false, length = 4)
    private String pin;

    /** 10-digit RFID card code scanned at the door kiosk. */
    @Column(name = "rfid_code", length = 20)
    private String rfidCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id")
    private MembershipPlan plan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** Surname / last name — set at reception, not editable by member. */
    @Column(length = 100)
    private String surname;

    /** Date of birth — set at reception, not editable by member. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Base64-encoded reception photo — set at registration, not editable by member. */
    @Lob
    @Column(name = "photo_data", columnDefinition = "TEXT")
    private String photoData;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Date the last "membership expiring soon" email was sent.
     * Used by NotificationScheduler to prevent duplicate emails per day.
     */
    @Column(name = "last_expiry_notification")
    private LocalDate lastExpiryNotificationSentAt;

    /**
     * Date the last "we miss you" email was sent.
     * Used by NotificationScheduler to prevent spamming inactive members.
     */
    @Column(name = "last_missed_notification")
    private LocalDate lastMissedNotificationSentAt;

    /**
     * Comma-separated list of {@code Notification.NotifType} names this member
     * does NOT want to receive. Empty / null means "all enabled".
     */
    @Column(name = "disabled_notif_types", length = 500)
    private String disabledNotifTypes;

    /** Gender — set at self-registration. Values: Female, Male, Other. */
    @Column(name = "gender", length = 20)
    private String gender;

    /** Internal admin note — visible only to staff, shown as tooltip in members list. */
    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    public Member() { }

    public Member(String name, String email, String phone, String pin,
                  MembershipPlan plan, LocalDate startDate, LocalDate expiryDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pin = pin;
        this.plan = plan;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
    }

    @Transient
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getRfidCode() { return rfidCode; }
    public void setRfidCode(String rfidCode) { this.rfidCode = rfidCode; }
    public MembershipPlan getPlan() { return plan; }
    public void setPlan(MembershipPlan plan) { this.plan = plan; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhotoData() { return photoData; }
    public void setPhotoData(String photoData) { this.photoData = photoData; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDate getLastExpiryNotificationSentAt() { return lastExpiryNotificationSentAt; }
    public void setLastExpiryNotificationSentAt(LocalDate d) { this.lastExpiryNotificationSentAt = d; }
    public LocalDate getLastMissedNotificationSentAt() { return lastMissedNotificationSentAt; }
    public void setLastMissedNotificationSentAt(LocalDate d) { this.lastMissedNotificationSentAt = d; }
    public String getDisabledNotifTypes() { return disabledNotifTypes; }
    public void setDisabledNotifTypes(String disabledNotifTypes) { this.disabledNotifTypes = disabledNotifTypes; }

    /** Returns true if this member has opted OUT of the given notification type. */
    @Transient
    public boolean isNotifTypeDisabled(String typeName) {
        if (disabledNotifTypes == null || disabledNotifTypes.isBlank() || typeName == null) return false;
        for (String t : disabledNotifTypes.split(",")) {
            if (typeName.equals(t.trim())) return true;
        }
        return false;
    }
}
