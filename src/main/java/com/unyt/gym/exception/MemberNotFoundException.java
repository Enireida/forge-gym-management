package com.unyt.gym.exception;

/**
 * Thrown when a member lookup by id, email, or PIN returns no record.
 * <p>Mapped to HTTP 404 by the global exception handler.</p>
 *
 * <p><b>Course requirement:</b> R10 Exception Handling — domain-specific
 * unchecked exception extending {@link GymException}.</p>
 */
public class MemberNotFoundException extends GymException {

    public MemberNotFoundException(Long id) {
        super("Member with id " + id + " was not found", 404);
    }

    public MemberNotFoundException(String identifier) {
        super("Member identified by '" + identifier + "' was not found", 404);
    }
}
