package com.haduy.ecommerce.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PaymentInitResponse {
    private UUID paymentId;
    private String paymentUrl;  // redirect client sang cổng thanh toán
    private String provider;
}