package com.unyt.gym.exception;

/**
 * Base unchecked exception for all domain errors raised by the gym
 * management system.
 *
 * <p>Extending {@link RuntimeException} keeps the service-layer signatures
 * clean while still letting the global exception handler translate the
 * error into a meaningful HTTP response.</p>
 *
 * <p><b>Course requirement:</b> R10 Exception Handling — base of the custom
 * exception hierarchy used throughout the project.</p>
 */
public class GymException extends RuntimeException {

    /** HTTP-style status code attached to the error (defaults to 500). */
    private final int statusCode;

    public GymException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public GymException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public GymException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
