package com.unyt.gym.pattern;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link PaymentReceiptBuilder} (Builder pattern, R9).
 *
 * <p>Also exercises R10 by verifying that invalid input causes the
 * expected {@link IllegalStateException} from {@code build()}.</p>
 */
class PaymentReceiptBuilderTest {

    @Test
    void build_minimalRequiredFields() {
        PaymentReceiptBuilder.PaymentReceipt r = new PaymentReceiptBuilder()
                .member("Elira Krasniqi")
                .amount(new BigDecimal("40.00"))
                .build();

        assertEquals("Elira Krasniqi", r.memberName());
        assertEquals(new BigDecimal("40.00"), r.amount());
        assertEquals("Cash", r.method());                  // default
        assertEquals(LocalDate.now(), r.date());           // default
        assertEquals(BigDecimal.ZERO, r.discount());       // default
    }

    @Test
    void total_subtractsDiscountFromAmount() {
        PaymentReceiptBuilder.PaymentReceipt r = new PaymentReceiptBuilder()
                .member("Arben")
                .amount(new BigDecimal("100.00"))
                .discount(new BigDecimal("15.00"))
                .build();

        assertEquals(new BigDecimal("85.00"), r.total());
    }

    @Test
    void build_missingMemberName_throws() {
        PaymentReceiptBuilder builder = new PaymentReceiptBuilder()
                .amount(new BigDecimal("40.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().toLowerCase().contains("member"));
    }

    @Test
    void build_negativeAmount_throws() {
        PaymentReceiptBuilder builder = new PaymentReceiptBuilder()
                .member("Driton")
                .amount(new BigDecimal("-10.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().toLowerCase().contains("non-negative")
                || ex.getMessage().toLowerCase().contains("amount"));
    }

    @Test
    void build_fluentChainingReturnsSameInstance() {
        PaymentReceiptBuilder b = new PaymentReceiptBuilder();
        assertSame(b, b.member("X"));
        assertSame(b, b.amount(BigDecimal.TEN));
        assertSame(b, b.method("Card"));
        assertSame(b, b.notes("note"));
    }

    @Test
    void receipt_isImmutableRecord() {
        PaymentReceiptBuilder.PaymentReceipt r = new PaymentReceiptBuilder()
                .member("Rea")
                .amount(new BigDecimal("50"))
                .method("Card")
                .build();

        // record => no setters; fields exposed only as accessors
        assertEquals("Card", r.method());
    }
}
