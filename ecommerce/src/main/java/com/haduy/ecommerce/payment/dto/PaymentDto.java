package com.haduy.ecommerce.payment.dto;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.common.enums.PaymentStatus;
import com.haduy.ecommerce.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PaymentDto {
    private UUID id;
    private UUID orderId;
    private PaymentProvider provider;
    private String providerRef;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant paidAt;

    public static PaymentDto from(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .provider(payment.getProvider())
                .providerRef(payment.getProviderRef())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}