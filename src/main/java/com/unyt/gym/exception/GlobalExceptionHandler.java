package com.unyt.gym.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Centralised exception translation layer.
 *
 * <p>Splits handling between REST endpoints (JSON body) and Thymeleaf
 * controllers (HTML error page) by inspecting the request URI. This is the
 * recommended Spring approach to honour the project's R10 requirement of
 * "no empty catch blocks" — every exception ends up logged and rendered
 * here in a single, audited place.</p>
 *
 * <p><b>Course requirement:</b> R10 Exception Handling.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link GymException} and its subclasses.
     */
    @ExceptionHandler(GymException.class)
    public Object handleGymException(GymException ex, HttpServletRequest request, Model model) {
        log.warn("Domain exception on {} - {}", request.getRequestURI(), ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(Map.of(
                            "timestamp", LocalDateTime.now(),
                            "status", ex.getStatusCode(),
                            "error", ex.getClass().getSimpleName(),
                            "message", ex.getMessage(),
                            "path", request.getRequestURI()));
        }
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", ex.getStatusCode());
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    /**
     * Handles checked {@link PaymentException} thrown by the payment layer.
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePayment(PaymentException ex,
                                                             HttpServletRequest request) {
        log.error("Payment failure ref={} on {}", ex.getPaymentReference(),
                request.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 402,
                        "error", "PaymentException",
                        "message", ex.getMessage(),
                        "reference", ex.getPaymentReference() == null ? "" : ex.getPaymentReference(),
                        "path", request.getRequestURI()));
    }

    /**
     * Catch-all for unanticipated errors — never an empty catch.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now(),
                            "status", 500,
                            "error", ex.getClass().getSimpleName(),
                            "message", ex.getMessage() == null ? "Unexpected error" : ex.getMessage(),
                            "path", request.getRequestURI()));
        }
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", 500);
        mv.addObject("message", "An unexpected error occurred. Please try again.");
        return mv;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }
}
