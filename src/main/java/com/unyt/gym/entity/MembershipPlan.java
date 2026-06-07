package com.unyt.gym.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * A subscription plan offered by the gym (Basic, Standard, Premium...).
 *
 * <p><b>Course requirement:</b> R7 JPA / ORM.</p>
 */
@Entity
@Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Plan length in months (1, 3, 6, 12...). */
    @Positive
    @Column(name = "duration_months", nullable = false)
    private int durationMonths;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Plan tier perk: members on this plan may use the RFID card to enter. */
    @Column(name = "allow_rfid", nullable = false)
    private boolean allowRfid = false;

    /** Plan tier perk: members on this plan may use the AI Coach. */
    @Column(name = "allow_ai_coach", nullable = false)
    private boolean allowAiCoach = false;

    public MembershipPlan() { }

    public MembershipPlan(String name, String description, BigDecimal price, int durationMonths) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMonths = durationMonths;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isAllowRfid() { return allowRfid; }
    public void setAllowRfid(boolean allowRfid) { this.allowRfid = allowRfid; }
    public boolean isAllowAiCoach() { return allowAiCoach; }
    public void setAllowAiCoach(boolean allowAiCoach) { this.allowAiCoach = allowAiCoach; }
}
