package com.haduy.ecommerce.payment.dto;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentInitRequest {
    @NotNull(message = "Đơn hàng không được để trống")
    private UUID orderId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentProvider provider;
}