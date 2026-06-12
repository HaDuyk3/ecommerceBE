package com.haduy.ecommerce.payment.provider;

import com.haduy.ecommerce.common.enums.PaymentProvider;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentGateway {

    PaymentProvider getProvider();

    String buildPaymentUrl(String providerRef, BigDecimal amount);

    /**
     * Query the gateway for the current status of a transaction.
     * Returns the raw status string ("SUCCESS" / "FAILED") or empty if the
     * gateway cannot be reached / does not support queries in this environment.
     */
    Optional<String> queryStatus(String providerRef);
}
