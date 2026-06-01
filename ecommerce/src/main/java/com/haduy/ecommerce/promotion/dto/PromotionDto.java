package com.haduy.ecommerce.promotion.dto;

import com.haduy.ecommerce.common.enums.PromotionStatus;
import com.haduy.ecommerce.common.enums.PromotionType;
import com.haduy.ecommerce.promotion.entity.Promotion;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PromotionDto {
    private UUID id;
    private UUID sellerProductId;
    private PromotionType type;
    private BigDecimal value;
    private Instant startAt;
    private Instant endAt;
    private PromotionStatus status;

    public static PromotionDto from(Promotion p) {
        return PromotionDto.builder()
                .id(p.getId())
                .sellerProductId(p.getSellerProduct().getId())
                .type(p.getType())
                .value(p.getValue())
                .startAt(p.getStartAt())
                .endAt(p.getEndAt())
                .status(p.getStatus())
                .build();
    }
}