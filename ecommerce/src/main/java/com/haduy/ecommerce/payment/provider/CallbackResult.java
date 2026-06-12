package com.haduy.ecommerce.payment.provider;

import java.math.BigDecimal;

public record CallbackResult(
        String providerRef,
        boolean success,
        BigDecimal amount
) {}
