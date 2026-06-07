package com.unyt.gym.pattern;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete {@link PricingStrategy} implementations.
 *
 * <p>Each method here is a pricing rule expressed as a lambda — which
 * also satisfies R3 (functional programming style).</p>
 *
 * <p><b>Course requirement:</b> R9 Design Patterns (Strategy concrete
 * implementations) and R3 Lambdas.</p>
 */
public final class PricingStrategies {

    private PricingStrategies() { }

    /** Pay full price — the default. */
    public static PricingStrategy regular() {
        return base -> base;
    }

    /** Apply a percentage discount. {@code 15} → 15% off. */
    public static PricingStrategy percentDiscount(double percent) {
        BigDecimal multiplier = BigDecimal.valueOf(1.0 - percent / 100.0);
        return base -> base.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /** Subtract a fixed amount, never going below zero. */
    public static PricingStrategy fixedDiscount(BigDecimal amount) {
        return base -> {
            BigDecimal result = base.subtract(amount);
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
        };
    }

    /** Student plan: 20% off. */
    public static PricingStrategy student() {
        return percentDiscount(20.0);
    }

    /** Annual plan: 2 months free → ~16.67% off. */
    public static PricingStrategy annualPromo() {
        return percentDiscount(16.67);
    }
}
