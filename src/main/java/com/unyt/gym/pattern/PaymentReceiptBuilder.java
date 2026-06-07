package com.unyt.gym.pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * <b>Builder Pattern</b> (GoF) — fluent construction of an immutable
 * {@code PaymentReceipt}. Useful because a receipt has many optional
 * fields (notes, discount, method, ...) and we want validated
 * construction without long telescoping constructors.
 *
 * <p><b>Course requirement:</b> R9 Design Patterns.</p>
 */
public final class PaymentReceiptBuilder {

    private String memberName;
    private String planName;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private String method = "Cash";
    private LocalDate date = LocalDate.now();
    private String notes;

    public PaymentReceiptBuilder member(String name) { this.memberName = name; return this; }
    public PaymentReceiptBuilder plan(String name)   { this.planName = name; return this; }
    public PaymentReceiptBuilder amount(BigDecimal v){ this.amount = v; return this; }
    public PaymentReceiptBuilder discount(BigDecimal v){ this.discount = v; return this; }
    public PaymentReceiptBuilder method(String m)    { this.method = m; return this; }
    public PaymentReceiptBuilder date(LocalDate d)   { this.date = d; return this; }
    public PaymentReceiptBuilder notes(String n)     { this.notes = n; return this; }

    public PaymentReceipt build() {
        if (memberName == null || memberName.isBlank())
            throw new IllegalStateException("memberName is required");
        if (amount == null || amount.signum() < 0)
            throw new IllegalStateException("amount must be non-negative");
        return new PaymentReceipt(memberName, planName, amount, discount, method, date, notes);
    }

    /** Immutable receipt produced by the builder. */
    public record PaymentReceipt(
            String memberName,
            String planName,
            BigDecimal amount,
            BigDecimal discount,
            String method,
            LocalDate date,
            String notes
    ) {
        public BigDecimal total() {
            return amount.subtract(discount == null ? BigDecimal.ZERO : discount);
        }
    }
}
