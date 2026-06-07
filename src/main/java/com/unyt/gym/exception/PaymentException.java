package com.unyt.gym.exception;

/**
 * Checked exception raised by the payment subsystem when a payment cannot
 * be processed (e.g. missing plan, expired card, validation failure).
 *
 * <p>This one is intentionally a <b>checked</b> exception — the requirement
 * (R10) explicitly asks for both checked and unchecked exceptions to be
 * used appropriately. Forcing callers to handle payment failures is the
 * responsible default for financial operations.</p>
 */
public class PaymentException extends Exception {

    private final String paymentReference;

    public PaymentException(String message) {
        super(message);
        this.paymentReference = null;
    }

    public PaymentException(String message, String paymentReference) {
        super(message);
        this.paymentReference = paymentReference;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.paymentReference = null;
    }

    public String getPaymentReference() {
        return paymentReference;
    }
}
