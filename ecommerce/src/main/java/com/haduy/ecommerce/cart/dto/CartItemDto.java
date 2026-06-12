package com.haduy.ecommerce.cart.dto;

import com.haduy.ecommerce.cart.entity.CartItem;
import com.haduy.ecommerce.cart.enums.CartItemStatus;
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
    private BigDecimal effectivePrice;      // current live price (for display diff)
    private BigDecimal snapshotPrice;       // price locked at add-to-cart time
    private BigDecimal snapshotShipping;
    private BigDecimal snapshotShippingDiscount;
    private BigDecimal itemTotal;           // snapshotPrice * quantity
    private CartItemStatus status;
    private boolean priceChanged;

    public static CartItemDto from(CartItem item) {
        var sp = item.getSellerProduct();
        BigDecimal livePrice = sp.getEffectivePrice();
        int stock = sp.getStock();

        CartItemStatus status;
        if (stock < item.getQuantity()) {
            status = CartItemStatus.OUT_OF_STOCK;
        } else if (item.getSnapshotPrice().compareTo(livePrice) != 0) {
            status = CartItemStatus.PRICE_CHANGED;
        } else {
            status = CartItemStatus.OK;
        }

        return CartItemDto.builder()
                .id(item.getId())
                .sellerProductId(sp.getId())
                .productName(sp.getProduct().getName())
                .shopName(sp.getSeller().getShopName())
                .quantity(item.getQuantity())
                .effectivePrice(livePrice)
                .snapshotPrice(item.getSnapshotPrice())
                .snapshotShipping(item.getSnapshotShipping())
                .snapshotShippingDiscount(item.getSnapshotShippingDiscount())
                .itemTotal(item.getSnapshotPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .status(status)
                .priceChanged(status != CartItemStatus.OK)
                .build();
    }
}
