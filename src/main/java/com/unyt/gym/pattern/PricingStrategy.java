package com.unyt.gym.pattern;

import java.math.BigDecimal;

/**
 * <b>Strategy Pattern</b> (GoF) — defines a family of pricing algorithms
 * (regular price, discounted price, student price, ...) and lets the
 * client pick one at runtime.
 *
 * <p>The actual algorithms live in
 * {@link com.unyt.gym.pattern.PricingStrategies}.</p>
 *
 * <p><b>Course requirement:</b> R9 Design Patterns.</p>
 */
@FunctionalInterface
public interface PricingStrategy {

    /**
     * @param basePrice the catalog price of the plan or PT package
     * @return the actual price the member must pay
     */
    BigDecimal apply(BigDecimal basePrice);
}
