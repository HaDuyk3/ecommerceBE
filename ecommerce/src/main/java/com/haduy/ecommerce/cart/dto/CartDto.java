package com.haduy.ecommerce.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CartDto {
    private UUID cartId;
    private List<CartItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal totalAmount;
}