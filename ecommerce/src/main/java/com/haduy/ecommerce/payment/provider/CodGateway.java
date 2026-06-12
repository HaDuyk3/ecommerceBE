package com.haduy.ecommerce.payment.provider;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CodGateway implements PaymentGateway {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.COD;
    }

    @Override
    public String buildPaymentUrl(String providerRef, BigDecimal amount) {
        return ""; // COD has no redirect URL
    }

    @Override
    public Optional<String> queryStatus(String providerRef) {
        return Optional.empty(); // COD has no gateway to query
    }
}
