package com.haduy.ecommerce.cart.dto;

import com.haduy.ecommerce.cart.entity.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CartItemDto {
    private UUID id;
    private UUID sellerProductId;
    private String productName;
    private String shopName;
    private Integer quantity;
    private BigDecimal effectivePrice;
    private BigDecimal snapshotPrice;
    private BigDecimal snapshotShipping;
    private BigDecimal itemTotal;

    public static CartItemDto from(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .sellerProductId(item.getSellerProduct().getId())
                .productName(item.getSellerProduct().getProduct().getName())
                .shopName(item.getSellerProduct().getSeller().getShopName())
                .quantity(item.getQuantity())
                .effectivePrice(item.getSellerProduct().getEffectivePrice())
                .snapshotPrice(item.getSnapshotPrice())
                .snapshotShipping(item.getSnapshotShipping())
                .itemTotal(item.getSnapshotPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}