package com.haduy.ecommerce.pricing.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PricingResult {
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal totalAmount;
}