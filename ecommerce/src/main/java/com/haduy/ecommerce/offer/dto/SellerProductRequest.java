package com.haduy.ecommerce.offer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class SellerProductRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private UUID productId;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal basePrice;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer stock;

    @Min(value = 0, message = "Phí ship không được âm")
    private BigDecimal baseShippingFee = BigDecimal.ZERO;
}