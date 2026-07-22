package com.xmesas.cucumberdiscount.domain;

import java.math.BigDecimal;

public record DiscountResult(
        BigDecimal subtotal,
        String tierLabel,
        BigDecimal tierDiscountAmount,
        boolean couponAccepted,
        BigDecimal couponDiscountAmount,
        BigDecimal total
) {
}
