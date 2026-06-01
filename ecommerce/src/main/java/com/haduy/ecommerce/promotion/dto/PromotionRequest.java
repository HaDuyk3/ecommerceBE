package com.haduy.ecommerce.promotion.dto;

import com.haduy.ecommerce.common.enums.PromotionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PromotionRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private UUID sellerProductId;

    @NotNull(message = "Loại khuyến mãi không được để trống")
    private PromotionType type;

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị phải lớn hơn 0")
    private BigDecimal value;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private Instant startAt;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private Instant endAt;
}