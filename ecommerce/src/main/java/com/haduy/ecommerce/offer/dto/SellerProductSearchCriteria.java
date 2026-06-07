package com.haduy.ecommerce.offer.dto;

import com.haduy.ecommerce.common.enums.SellerProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SellerProductSearchCriteria {

    private final UUID sellerId;
    private final UUID productId;
    private final SellerProductStatus status;
    private final Boolean inStock;
}
