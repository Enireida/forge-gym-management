package com.unyt.gym.entity;

/**
 * Enumeration of user roles supported by the gym system.
 *
 * <p>Used both as the persisted role discriminator on {@link User} and as
 * the high-level routing key in the web layer (admins go to admin pages,
 * trainers to trainer pages, etc.).</p>
 */
public enum Role {
    ADMIN,
    MANAGER,
    TRAINER,
    CLIENT;

    /**
     * Lower-case form used by the URL paths and templates.
     */
    public String slug() {
        return name().toLowerCase();
    }
}
