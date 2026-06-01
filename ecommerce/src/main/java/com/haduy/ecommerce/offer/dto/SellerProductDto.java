package com.haduy.ecommerce.offer.dto;

import com.haduy.ecommerce.common.enums.SellerProductStatus;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class SellerProductDto {
    private UUID id;
    private UUID sellerId;
    private String shopName;
    private UUID productId;
    private String productName;
    private BigDecimal basePrice;
    private BigDecimal effectivePrice;
    private Integer stock;
    private BigDecimal baseShippingFee;
    private SellerProductStatus status;

    public static SellerProductDto from(SellerProduct sp) {
        return SellerProductDto.builder()
                .id(sp.getId())
                .sellerId(sp.getSeller().getId())
                .shopName(sp.getSeller().getShopName())
                .productId(sp.getProduct().getId())
                .productName(sp.getProduct().getName())
                .basePrice(sp.getBasePrice())
                .effectivePrice(sp.getEffectivePrice())
                .stock(sp.getStock())
                .baseShippingFee(sp.getBaseShippingFee())
                .status(sp.getStatus())
                .build();
    }
}