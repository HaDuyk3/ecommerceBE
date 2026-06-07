package com.haduy.ecommerce.promotion.dto;

import com.haduy.ecommerce.promotion.enums.PromotionLifecycle;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PromotionSearchCriteria {

    private final UUID sellerUserId;
    private final UUID sellerProductId;
    private final PromotionLifecycle lifecycle;
}
