package com.xmesas.cucumberdiscount.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Business rules exist here in plain Java on purpose - the Gherkin feature files describe
 * the SAME rules in a form a non-engineer stakeholder can read and agree with, and the step
 * definitions call straight into this class with no logic duplicated in the test layer.
 */
@Component
public class DiscountEngine {

    private static final Map<String, BigDecimal> COUPONS = Map.of(
            "SAVE10", new BigDecimal("10.00"),
            "SAVE25", new BigDecimal("25.00")
    );

    public DiscountResult calculate(BigDecimal subtotal, String couponCode) {
        BigDecimal tierRate = tierRateFor(subtotal);
        String tierLabel = tierLabelFor(tierRate);
        BigDecimal tierDiscount = subtotal.multiply(tierRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal afterTier = subtotal.subtract(tierDiscount);

        boolean couponAccepted = true;
        BigDecimal couponDiscount = BigDecimal.ZERO.setScale(2);

        if (couponCode != null && !couponCode.isBlank()) {
            BigDecimal flatAmount = COUPONS.get(couponCode);
            if (flatAmount == null) {
                couponAccepted = false;
            } else {
                couponDiscount = flatAmount.min(afterTier);
            }
        }

        BigDecimal total = afterTier.subtract(couponDiscount);

        return new DiscountResult(subtotal, tierLabel, tierDiscount, couponAccepted, couponDiscount, total);
    }

    private BigDecimal tierRateFor(BigDecimal subtotal) {
        if (subtotal.compareTo(new BigDecimal("200.00")) >= 0) {
            return new BigDecimal("0.15");
        }
        if (subtotal.compareTo(new BigDecimal("100.00")) >= 0) {
            return new BigDecimal("0.10");
        }
        if (subtotal.compareTo(new BigDecimal("50.00")) >= 0) {
            return new BigDecimal("0.05");
        }
        return BigDecimal.ZERO;
    }

    private String tierLabelFor(BigDecimal tierRate) {
        if (tierRate.compareTo(new BigDecimal("0.15")) == 0) {
            return "15 percent";
        }
        if (tierRate.compareTo(new BigDecimal("0.10")) == 0) {
            return "10 percent";
        }
        if (tierRate.compareTo(new BigDecimal("0.05")) == 0) {
            return "5 percent";
        }
        return "none";
    }
}
