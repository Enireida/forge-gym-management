package com.unyt.gym.exception;

/**
 * Thrown when an insert would violate a uniqueness constraint
 * (duplicate email, duplicate PIN, etc.). Mapped to HTTP 409.
 *
 * <p><b>Course requirement:</b> R10 Exception Handling — second domain
 * exception in the hierarchy.</p>
 */
public class DuplicateResourceException extends GymException {

    public DuplicateResourceException(String resource, String value) {
        super(resource + " '" + value + "' already exists", 409);
    }
}
