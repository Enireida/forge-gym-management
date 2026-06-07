package com.unyt.gym.pattern;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link PricingStrategies} concrete implementations.
 *
 * <p>Exercises R9 (Strategy pattern) and R3 (lambdas — every strategy
 * here is a lambda).</p>
 */
class PricingStrategiesTest {

    @Test
    void regular_returnsBasePriceUnchanged() {
        BigDecimal base = new BigDecimal("50.00");
        assertEquals(base, PricingStrategies.regular().apply(base));
    }

    @Test
    void percentDiscount_appliesPercentAndRoundsToTwoDecimals() {
        BigDecimal base = new BigDecimal("100.00");
        BigDecimal result = PricingStrategies.percentDiscount(15.0).apply(base);
        assertEquals(new BigDecimal("85.00"), result);
    }

    @Test
    void student_isTwentyPercentOff() {
        BigDecimal base = new BigDecimal("60.00");
        BigDecimal result = PricingStrategies.student().apply(base);
        assertEquals(new BigDecimal("48.00"), result);
    }

    @Test
    void annualPromo_givesAboutSixteenPercentOff() {
        BigDecimal base = new BigDecimal("600.00");
        BigDecimal result = PricingStrategies.annualPromo().apply(base);
        // 600 * (1 - 0.1667) ~ 499.98 (rounding)
        assertTrue(result.compareTo(new BigDecimal("498.00")) > 0);
        assertTrue(result.compareTo(new BigDecimal("502.00")) < 0);
    }

    @Test
    void fixedDiscount_neverGoesBelowZero() {
        BigDecimal result = PricingStrategies
                .fixedDiscount(new BigDecimal("100"))
                .apply(new BigDecimal("30"));
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    void fixedDiscount_normalCase() {
        BigDecimal result = PricingStrategies
                .fixedDiscount(new BigDecimal("10"))
                .apply(new BigDecimal("40"));
        assertEquals(new BigDecimal("30"), result);
    }
}
