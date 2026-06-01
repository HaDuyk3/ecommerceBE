package com.haduy.ecommerce.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCallbackRequest {
    @NotBlank
    private String providerRef;

    @NotBlank
    private String status; // "SUCCESS" hoặc "FAILED" từ gateway
}