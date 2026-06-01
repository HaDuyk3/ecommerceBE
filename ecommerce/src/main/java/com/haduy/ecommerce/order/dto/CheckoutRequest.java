package com.haduy.ecommerce.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CheckoutRequest {
    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    private UUID addressId;
}